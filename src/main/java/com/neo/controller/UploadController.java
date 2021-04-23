package com.neo.controller;

import com.neo.entity.Files;
import com.neo.exception.LargeFileException;
import com.neo.service.FilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Anonymous
 */
@Controller
public class UploadController {
    /**
     * 保存文件到指定目录
     */
    private static final Long MAX_UPLOAD_SIZE = 20971520L;

    @Autowired
    FilesService fs;

    @GetMapping("/")
    public String index() {
        return "upload";
    }

    /**
     * 处理upload请求,返回uploadStatus.html
     *
     * @param file               表单提交的文件,取回并赋值给file
     * @param redirectAttributes 跳转
     * @return 跳转网页: uploadStatus附带message
     * 文件为空: message = "Please select a file to upload"
     * 文件不为空:
     * 文件上传成功: message = "You successfully uploaded '" + file.getOriginalFilename() + "'"
     * 文件上传失败: message = "Server throw IOException"
     */
    @PostMapping("/upload")
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "请选择文件再上传!");
            return "redirect:uploadStatus";
        }

        try {
            // 取得文件并以Bytes方式保存
            byte[] bytes = file.getBytes();
            if (bytes.length > MAX_UPLOAD_SIZE) {
                throw new LargeFileException(MAX_UPLOAD_SIZE);
            }
            String uuid = UUID.randomUUID().toString();
            fs.save(new Files(uuid, file.getOriginalFilename(), file.getContentType(), bytes));
            redirectAttributes.addFlashAttribute("message",
                    "文件'" + file.getOriginalFilename() + "'上传成功!");

        } catch (LargeFileException largeFileException) {
            redirectAttributes.addFlashAttribute("message", String.format(
                    "文件过大,上传失败!请将文件控制在%dMB内!",
                    largeFileException.getMaxSize() / 1048576
            ));
        } catch (IOException s) {
            redirectAttributes.addFlashAttribute("message", "上传失败!");
            s.printStackTrace();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", e.toString());
            e.printStackTrace();
        }
        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }

}
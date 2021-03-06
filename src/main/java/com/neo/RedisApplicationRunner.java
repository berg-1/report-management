package com.neo;

import com.neo.domain.Classes;
import com.neo.domain.Course;
import com.neo.service.ClassesService;
import com.neo.service.CourseService;
import com.neo.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
@Slf4j
public class RedisApplicationRunner implements ApplicationRunner {

    final RedisService redisService;

    final CourseService courseService;

    final ClassesService classesService;

    public RedisApplicationRunner(RedisService redisService, CourseService courseService, ClassesService classesService) {
        this.redisService = redisService;
        this.courseService = courseService;
        this.classesService = classesService;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing Redis Courses");
        InitRedisCourse();
        log.info("Initializing Redis Classes");
        InitRedisClasses();
    }

    private void InitRedisClasses() {
        List<Classes> classesList = classesService.list();
        for (Classes classes : classesList) {
            redisService.hSet("classes", classes.getCid(), classes.getName());
        }
    }

    private void InitRedisCourse() {
        List<Course> courseList = courseService.list();
        for (Course course : courseList) {
            redisService.hSet("course", course.getCourseId(), course.getName());
        }
    }
}

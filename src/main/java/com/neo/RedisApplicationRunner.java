package com.neo;

import com.neo.domain.Classes;
import com.neo.domain.Course;
import com.neo.service.ClassesService;
import com.neo.service.CourseService;
import com.neo.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
@Slf4j
public class RedisApplicationRunner implements ApplicationRunner {

    @Autowired
    RedisService redisService;

    @Autowired
    CourseService courseService;

    @Autowired
    ClassesService classesService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Initializing Redis Courses");
        InitRedisCourse();
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

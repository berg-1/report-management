package com.neo.controller;

import com.neo.domain.Node;
import com.neo.service.ClassesCourseService;
import com.neo.service.ClassesService;
import com.neo.service.CourseService;
import com.neo.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Berg
 */
@RestController
public class NodesController {

    @Autowired
    TeacherService teacherService;

    @Autowired
    ClassesService classesService;

    @Autowired
    CourseService courseService;

    @Autowired
    ClassesCourseService classesCourseService;

    @RequestMapping("nodes")
    @GetMapping
    public List<Node> nodes() {
        return getSampleNodeList();
    }


    private List<Node> getSampleNodeList() {
        ArrayList<Node> nodes = new ArrayList<>();

        nodes.add(new Node("Root", "0", "Root Node", "https://frontbackend.com/child1"));
        nodes.add(new Node("Child1", "Root", "Child1", "https://frontbackend.com/child1"));
        nodes.add(new Node("Child2", "Root", "Child2", "https://frontbackend.com/child2"));
        nodes.add(new Node("Child3", "Root", "Child3", "https://frontbackend.com/child3"));
        nodes.add(new Node("Child3.1", "Child3", "Child3.1", "https://frontbackend.com/child3/child1"));
        nodes.add(new Node("Child3.2", "Child3", "Child3.2", "https://frontbackend.com/child3/child2"));
        nodes.add(new Node("Child4", "Root", "Child4", "https://frontbackend.com/child4"));
        return nodes;
    }

}

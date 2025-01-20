package com.ashcollege.controllers;

import com.ashcollege.entities.ExerciseEntity;
import com.ashcollege.entities.SubjectEntity;
import com.ashcollege.service.Persist;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class GeneralController {

    @Autowired
    private Persist persist;

@PostConstruct
public void init (){
    SubjectEntity equations = new SubjectEntity("משוואות - בסיס");
    SubjectEntity twoVariablesEquations = new SubjectEntity("שתי משוואות עם שני נעלמים");
    SubjectEntity verbalProblems = new SubjectEntity("בעיות מילוליות");
    SubjectEntity derivatives = new SubjectEntity("נגזרות");
    SubjectEntity integrals = new SubjectEntity("אינטגרלים");
    SubjectEntity powers = new SubjectEntity("חוקי חזקות");
    SubjectEntity exponentialFunctions  = new SubjectEntity("פונקציות מעריכיות");

    persist.save(equations);persist.save(twoVariablesEquations);persist.save(verbalProblems);
    persist.save(derivatives);persist.save(integrals);persist.save(powers);persist.save(exponentialFunctions);

    ExerciseEntity exercise = new ExerciseEntity();
    exercise.setExerciseName("ex1");
    exercise.setSubject(equations);
    persist.save(exercise);

    ExerciseEntity exercise2 = persist.loadObject(ExerciseEntity.class,1);
    System.out.println();
}

    @RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
    public Object hello() {
        return "Hello From Server";
    }



}

package com.tanhua.server.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tanhua.common.mapper.QuestionMapper;
import com.tanhua.common.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;


    public Question queryQuestion(Long userId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id", userId);
        return this.questionMapper.selectOne(queryWrapper);
    }

    public void save(Long userId, String content) {
        //查询陌生人问题
        Question question = this.queryQuestion(userId);
        if(null != question){
            question.setTxt(content);
            question.setUpdated(new Date());
            this.questionMapper.updateById(question);
        }else {
            question = new Question();
            question.setUserId(userId);
            question.setTxt(content);
            question.setCreated(new Date());
            question.setUpdated(question.getCreated());
            this.questionMapper.insert(question);
        }
    }
}
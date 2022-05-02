package com.taoyyz.framework.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.LocalDateTimeUtil;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.model.entity.News;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 22:35
 */
@RestController
@RequestMapping("news")
@Slf4j
public class NewsController {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ApplicationContext context;

    /**
     * 获取新闻列表，从redis缓存中
     *
     * @throws JsonProcessingException 如果反序列化新闻失败
     */
    @GetMapping("listNews")
    public Result listNews() throws JsonProcessingException {
        Map<Object, Object> newsMap = redisUtil.hGetAll(CommonRedisKey.NEWS_KEY);
        if (newsMap.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<News> newsList = objectMapper.readValue((String) newsMap.get("list"), new TypeReference<List<News>>() {
        });
        return Result.success(newsList).put("time", newsMap.get("time"));
    }

    /**
     * 初始化新闻列表到redis
     *
     * @throws JsonProcessingException 如果序列化新闻失败
     */
    @PostConstruct
    @Async
    void initNews() throws JsonProcessingException {
        Document document;
        try {
            document = Jsoup.connect("https://www.suse.edu.cn/p/10/?StId=st_app_news_search_x636053927299964918_x__x__x_0_x_0_x_news")
                    .userAgent("Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79" +
                            " Safari/535.12").timeout(3000).get();
        } catch (IOException e) {
            //获取学校新闻失败
            log.error("获取新闻失败：连接超时");
            return;
        }
        Elements elements = document.getElementsByClass("div_item");
        //取前10条新闻
        ArrayList<News> newsList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            Element newsItem = elements.get(i);
            String date = newsItem.getElementsByClass("other").get(0).text();
            Element element = newsItem.getElementsByTag("a").get(0);
            News news = new News();
            news.setNewsDate(date);
            news.setNewsTitle(element.text());
            String elementString = element.toString();
            news.setNewsUrl("https://www.suse.edu.cn" + elementString.substring(elementString.indexOf("href=\"") + 6, elementString.indexOf("\">")));
            newsList.add(news);
        }
        String newsString = objectMapper.writeValueAsString(newsList);
        redisUtil.hPut(CommonRedisKey.NEWS_KEY, "list", newsString);
        redisUtil.hPut(CommonRedisKey.NEWS_KEY, "time", LocalDateTimeUtil.convertToString(LocalDateTime.now()));
        log.info("初始化新闻列表成功");
    }

    /**
     * 每隔600000毫秒（10分钟）定时刷新新闻列表
     *
     * @throws JsonProcessingException 如果序列化新闻失败
     */
    @Scheduled(fixedDelay = 600_000L)
    void refreshNews() throws JsonProcessingException {
        NewsController bean = context.getBean(NewsController.class);
        bean.initNews();
    }
}

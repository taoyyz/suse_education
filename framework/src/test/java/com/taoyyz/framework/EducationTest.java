package com.taoyyz.framework;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.web.mapper.UserMapper;
import com.taoyyz.framework.web.model.entity.News;
import com.taoyyz.framework.web.model.entity.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/3 22:24
 */
//@SpringBootTest
public class EducationTest {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserMapper userMapper;

    @Test
    public void test1() {
        System.out.println(encoder.encode("qwer1111"));
    }

    @Test
    public void test2() throws IOException {
        Document document = Jsoup
                .connect("https://www.suse.edu.cn/p/10/?StId=st_app_news_search_x636053927299964918_x__x__x_0_x_0_x_news")
                .userAgent("Mozilla/5.0 (Windows NT 5.1; zh-CN) AppleWebKit/535.12 (KHTML, like Gecko) Chrome/22.0.1229.79" +
                        " Safari/535.12").timeout(5000).get();
        Elements elements = document.getElementsByClass("div_item");
        //取前10条新闻
        ArrayList<News> newsList = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            String date = elements.get(i).getElementsByClass("other").get(0).text();
            Element element = elements.get(i).getElementsByTag("a").get(0);
            News news = new News();
            news.setNewsDate(date);
            news.setNewsTitle(element.text());
            String elementString = element.toString();
            news.setNewsUrl("https://www.suse.edu.cn" + elementString.substring(elementString.indexOf("href=\"") + 6, elementString.indexOf("\">")));
            newsList.add(news);
        }
        newsList.forEach(System.out::println);
    }

    @Test
    public void test3() throws JsonProcessingException {
        News news1 = new News().setNewsDate("2022-11-21").setNewsTitle("新闻1").setNewsUrl("http://www.baidu.com");
        News news2 = new News().setNewsDate("2022-11-22").setNewsTitle("新闻2").setNewsUrl("http://www.baidu.com");
        ArrayList<News> news = new ArrayList<>();
        news.add(news1);
        news.add(news2);
        redisUtil.set(CommonRedisKey.NEWS_KEY, objectMapper.writeValueAsString(news));
    }

    @Test
    public void test4() {
        List<User> users = userMapper.selectList(null);
        System.out.println("users = " + users);
    }

    @Test
    public void test5() {
        new Thread(() -> {
            System.out.println("线程1启动");
            RLock lock = redissonClient.getLock("lock.3");
            boolean locked = false;
            try {
                locked = lock.tryLock(5L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (locked) {
                try {
                    System.out.println("线程1锁住");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                    System.out.println("线程1解锁");
                }
            } else {
                System.out.println("1没获取到锁");
            }
        }).start();
        new Thread(() -> {
            System.out.println("线程2启动");
            RLock lock = redissonClient.getLock("lock.3");
            boolean locked = false;
            try {
                locked = lock.tryLock(5L, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (locked) {
                try {
                    System.out.println("线程2锁住");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                    System.out.println("线程2解锁");
                }
            } else {
                System.out.println("2没获取到锁");
            }
        }).start();
    }
}

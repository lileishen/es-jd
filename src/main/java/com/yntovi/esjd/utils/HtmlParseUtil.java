package com.yntovi.esjd.utils;

import com.yntovi.esjd.pojo.Good;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HtmlParseUtil {

    public static List<Good> parseJd(String keyword) throws IOException {
        //        获取请求
        String url ="https://search.jd.com/Search?keyword="+keyword;
//        解析网页
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements tags = element.getElementsByTag("li");
        List<Good> goods = new ArrayList<>();
        for (Element tag : tags) {
            String img = tag.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = tag.getElementsByClass("p-price").eq(0).text();
            String name = tag.getElementsByClass("p-name").eq(0).text();
            Good good =new Good(img,name,price);
            goods.add(good);
        }
        return goods;
    }


    public static void main(String[] args) throws IOException {
        parseJd("心理学").forEach(System.out::println);
    }
}

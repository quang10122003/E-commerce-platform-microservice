package com.example.producr_service.adapter.out.embedding;

import com.example.producr_service.application.port.out.embedding.HtmlTextExtractorPort;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupHtmlTextExtractorAdapter implements HtmlTextExtractorPort {
    @Override
    public String toPlainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document document = Jsoup.parseBodyFragment(html);
        document.select("script, style").remove();
        return document.body().text();
    }
}

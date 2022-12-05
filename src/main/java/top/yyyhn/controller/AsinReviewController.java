package top.yyyhn.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.ApiOperation;
import org.assertj.core.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RestController;
import top.yyyhn.entity.AsinReview;
import top.yyyhn.service.AsinReviewService;
import top.yyyhn.util.AmazonReviewCrawl;
import us.codecraft.webmagic.Spider;

import java.util.List;
import java.util.UUID;

import static top.yyyhn.util.AmazonReviewCrawl.asinsToUrls;

/**
 * 前端控制器
 *
 * @author liutaohua
 * @since 2022-12-04
 */
@RestController
public class AsinReviewController {

    @Autowired
    private AsinReviewService asinReviewService;

    @ApiOperation(value = "爬取评论")
    @GetMapping("/crawl")
    public void crawl(String asins) {
        // 前端输入asin值以空格分开，拼接出url，传递后开始爬取
        String[] asinUrls = asinsToUrls(asins);
        // 开启50个线程开始爬取Amazon评论
        Spider.create(new AmazonReviewCrawl(asinReviewService))
                .addUrl(asinUrls)
                .thread(50)
                .start();
    }

    @ApiOperation(value = "导出Excel")
    @PostMapping("/exportExcel")
    public String exportExcel(String path, String asins) {
        // 输入asins以空格符分开如XXXXX XXXXX，输入路径如C:\Users\admin
        ExcelWriter excelWriter = null;
        try {
            QueryWrapper<AsinReview> asinReviewQueryWrapper = new QueryWrapper<>();
            List<Object> asinList = Arrays.asList(asins.split(" "));
            asinReviewQueryWrapper.in("asin", asinList);
            // 筛选ASIN
            List<AsinReview> list = asinReviewService.list(asinReviewQueryWrapper);
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            // 拼接导出Excel路径文件前缀
            String finalPath = path + "\\AmazonReview-" + uuid + ".xlsx";
            excelWriter = EasyExcel.write(finalPath, AsinReview.class).build();
            WriteSheet writeSheet = EasyExcel.writerSheet("AsinReview").build();
            excelWriter.write(list, writeSheet);
            return "导出成功，导出路径：" + finalPath;
        } catch (Exception e) {
            return e.getMessage() + "\n导出失败";
        } finally {
            // 关闭流
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
}


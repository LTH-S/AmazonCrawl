package top.yyyhn.util;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import top.yyyhn.entity.AsinReview;
import top.yyyhn.service.AsinReviewService;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: liutaohua
 * @Date: 2022/12/4
 */
@Component
public class AmazonReviewCrawl implements PageProcessor {

    private final AsinReviewService asinReviewService;

    public AmazonReviewCrawl(AsinReviewService asinReviewService) {
        this.asinReviewService = asinReviewService;
    }

    /**
     * 前缀
     */
    private static final String PREFIX_URL = "https://www.amazon.com/product-reviews/";

    /**
     * 中缀
     */
    private static final String INFIX_URL = "/ref=cm_cr_arp_d_viewopt_sr?ie=UTF8&filterByStar=";

    /**
     * 后缀
     */
    private static final String SUFFIX_URL = "&reviewerType=all_reviews&formatType=current_format&pageNumber=1#reviews-filter-bar";

    /**
     * 锁对象
     */
    private final Object obj = new Object();

    /**
     * 所有星星
     */
    private static final String ALL_STAR = "all_stars";
    /**
     * 一个星星
     */
    private static final String ONE_STAR = "one_star";
    /**
     * 两个星星
     */
    private static final String TWO_STAR = "two_star";
    /**
     * 三个星星
     */
    private static final String THREE_STAR = "three_star";
    /**
     * 四个星星
     */
    private static final String FOUR_STAR = "four_star";
    /**
     * 五个星星
     */
    private static final String FIVE_STAR = "five_star";


    @Override
    public void process(Page page) {
        Html html = page.getHtml();
        // 星星数量和颜色尺码
        String starAndColor = html.xpath("//*[@id=\"reviews-filter-info-segment\"]/text()").get();
        String[] split = starAndColor.split(",");
        String star;
        String color;
        // 由于全部星星的情况，这个元素只显示颜色尺码不显示星星数，这里分情况
        if (split.length == 2) {
            star = starAndColor.split(",")[0];
            color = starAndColor.split(",")[1];
        } else {
            color = starAndColor.split(",")[0];
            star = "All star";
        }
        // 获取当前星级的评论数
        String review = html.xpath("//*[@id=\"filter-info-section\"]/div[2]/text()").get();
        // 获得当前ASIN
        String asin = StringUtils.substringBefore(StringUtils.substringAfter(html.xpath("//*[@id=\"cm_cr-product_info\"]/div/div[2]/div/div/div[2]/div[1]/h1/a/@href").get(), "/dp/"), "/ref");
        QueryWrapper<AsinReview> asinReviewQueryWrapper = new QueryWrapper<>();
        asinReviewQueryWrapper.eq("asin", asin);
        synchronized (obj) {
            AsinReview asinReview = asinReviewService.getOne(asinReviewQueryWrapper);
            // 如果没有这个ASIN值，则插入一条新的asinReview
            if (asinReview == null) {
                asinReview = new AsinReview();
                asinReview.setColor(color);
                asinReview.setAsin(asin);
                asinReviewService.save(asinReview);
            }
            // 如果数据库中有这个ASIN值，则进行更新行的操作，判断星级，放入不同的列中
            switch (star) {
                case "5 star":
                    asinReview.setFiveStar(review);
                    break;
                case "4 star":
                    asinReview.setFourStar(review);
                    break;
                case "3 star":
                    asinReview.setThreeStar(review);
                    break;
                case "2 star":
                    asinReview.setTwoStar(review);
                    break;
                case "1 star":
                    asinReview.setOneStar(review);
                    break;
                default:
                    asinReview.setTotal(review);
            }
            asinReviewQueryWrapper = new QueryWrapper<>();
            asinReviewQueryWrapper.eq("asin", asin);
            asinReviewService.update(asinReview, asinReviewQueryWrapper);
        }
    }

    @Override
    public Site getSite() {
        // 由于是美国站点，这里设置长一些，超时时间为200s
        return Site.me().setTimeOut(200000);
    }

    public static String[] asinsToUrls(String asins) {
        // 切割asins
        String[] asinList = asins.split(" ");
        List<String> asinUrlList = new ArrayList<>();
        // 封装asinUrlList，每个asin有6个url
        for (String asin : asinList) {
            String asinAllStarUrl = PREFIX_URL + asin + INFIX_URL + ALL_STAR + SUFFIX_URL;
            String asinOneStarUrl = PREFIX_URL + asin + INFIX_URL + ONE_STAR + SUFFIX_URL;
            String asinTwoStarUrl = PREFIX_URL + asin + INFIX_URL + TWO_STAR + SUFFIX_URL;
            String asinThreeStarUrl = PREFIX_URL + asin + INFIX_URL + THREE_STAR + SUFFIX_URL;
            String asinFourStarUrl = PREFIX_URL + asin + INFIX_URL + FOUR_STAR + SUFFIX_URL;
            String asinFiveStarUrl = PREFIX_URL + asin + INFIX_URL + FIVE_STAR + SUFFIX_URL;
            asinUrlList.add(asinAllStarUrl);
            asinUrlList.add(asinOneStarUrl);
            asinUrlList.add(asinTwoStarUrl);
            asinUrlList.add(asinThreeStarUrl);
            asinUrlList.add(asinFourStarUrl);
            asinUrlList.add(asinFiveStarUrl);
        }
        return asinUrlList.toArray(new String[0]);
    }
}

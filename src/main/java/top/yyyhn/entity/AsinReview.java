package top.yyyhn.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.Date;
import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * AsinReview实体类对象
 *
 * @author liutaohua
 * @since 2022-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AsinReview对象")
public class AsinReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String asin;

    private String color;

    private String total;

    private String fiveStar;

    private String fourStar;

    private String threeStar;

    private String twoStar;

    private String oneStar;

    private Date createTime;

    private Date updateTime;

}

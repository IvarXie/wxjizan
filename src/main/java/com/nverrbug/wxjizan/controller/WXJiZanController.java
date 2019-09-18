package com.nverrbug.wxjizan.controller;

import com.nverrbug.wxjizan.constant.JiZanConstant;
import com.nverrbug.wxjizan.jizan.WxJiZanImg;
import com.nverrbug.wxjizan.ocr.TencentOcr;
import com.nverrbug.wxjizan.utils.FileToBase64;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Title: 演示controller
 * Description: </p>
 * Email is Wenbo.Xie@b-and-qchina.com<
 * Company: http://www.bthome.com
 *
 * @author xie.wenbo
 * @date 2019-04-03 13:41
 */
@Api(value="/wx", tags="微信图片生成接口")
@RestController
@RequestMapping("/wx")
public class WXJiZanController {
    private static final Logger logger = LoggerFactory.getLogger(WXJiZanController.class);

    @ApiOperation(value = "生成微信集赞图片", httpMethod = "POST", position = 1)
    @RequestMapping(value = "/jizan/uploadImage",method= RequestMethod.POST, produces = "application/json; charset=utf-8")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name="praiseNum",value="集赞数量",dataType="int", paramType = "query",defaultValue = "10"),
            @ApiImplicitParam(name="bnqPraiseNum",value="百安居赞数量",dataType="int",defaultValue = "5"),
            @ApiImplicitParam(name="systemTime",value="系统时间",dataType="string"),
            @ApiImplicitParam(name="publishTime",value="发布时间",dataType="string")})
    public Map<String,Object> uploadImageJizan(MultipartFile file,
                                               Integer praiseNum,
                                               Integer bnqPraiseNum,
                                               String systemTime,
                                               String publishTime,
                                               HttpServletResponse response){
        logger.info("生成微信集赞图片 praiseNum:{},bnqPraiseNum:{}",praiseNum,bnqPraiseNum);
        Map<String,Object> map = new HashMap<>();
        map.put("code",200);
        map.put("message","ok");
        if(file.isEmpty()){
            map.put("code",500);
            map.put("message","图片未上传");
            return map;
        }
        if(praiseNum == null || praiseNum ==0){
            map.put("code",501);
            map.put("message","集赞数量不能为空或者0");
            return map;
        }else{
            if(praiseNum>JiZanConstant.avatarNums){
                map.put("code",505);
                map.put("message","集赞数量不能超过"+JiZanConstant.avatarNums);
                return map;
            }
        }
        if(bnqPraiseNum != null && bnqPraiseNum > praiseNum){
            map.put("code",502);
            map.put("message","百安居点赞数量不能超过总数");
            return map;
        }
        try {
            TencentOcr tencentOcr = new TencentOcr(FileToBase64.getBase64FromInputStream(new ByteArrayInputStream(file.getBytes())));
            WxJiZanImg wxJiZanImg = new WxJiZanImg();
            ByteArrayOutputStream outputStream = wxJiZanImg.generateImg(new ByteArrayInputStream(file.getBytes()),tencentOcr,praiseNum,bnqPraiseNum,systemTime,publishTime);
            String fileName = "jizan_"+System.currentTimeMillis()+".jpg";
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            outputStream.writeTo(response.getOutputStream());
            logger.info("生成完成");
        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            map.put("code",503);
            map.put("message","文字识别错误");
        } catch (IOException e) {
            e.printStackTrace();
            map.put("code",504);
            map.put("message","IO异常");
        }
        return map;
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 11:24
     * @Description 克隆输入流
     * @param input 图片输入流
     * @return java.io.ByteArrayOutputStream
     */
    private static ByteArrayOutputStream cloneInputStream(InputStream input) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            return baos;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

package com.nverrbug.wxjizan.ocr;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.Coord;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.TextDetection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Title: 腾讯ocr识别
 * Description: </p>
 * Email is Wenbo.Xie@b-and-qchina.com<
 * Company: http://www.bthome.com
 *
 * @author xie.wenbo
 * @date 2019-09-17 09:21
 */
public class TencentOcr {
    private static final Logger logger = LoggerFactory.getLogger(TencentOcr.class);
    private Coord[] deleteCoords;
    private Coord[] timeCoords;
    private void ocr(String imageBase64) throws TencentCloudSDKException {
        Credential cred = new Credential("AKIDoshGdlxjhGfw8AtVnD3i8MTUGfEToQ8a", "ZAVeYnsiIf1DAtNddNKUq0wd6NL4Xopa");

        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("ocr.tencentcloudapi.com");

        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);

        OcrClient client = new OcrClient(cred, "ap-beijing", clientProfile);

        String params = "{\"ImageBase64\":\""+imageBase64+"\"}";
        GeneralBasicOCRRequest req = GeneralBasicOCRRequest.fromJsonString(params, GeneralBasicOCRRequest.class);

        /**
         *
         */
        GeneralBasicOCRResponse resp = client.GeneralBasicOCR(req);
        TextDetection[] textDetections = resp.getTextDetections();
        boolean flag = true;
        Pattern pattern= Pattern.compile("\\d{1,2}:\\d{2}");
        for(int i = 0 ;i<textDetections.length;i++){
            Matcher matcher = pattern.matcher(textDetections[i].getDetectedText().trim());
            boolean timeFlag = matcher.find();
            if(timeFlag && flag){
                logger.info("匹配到系统时间 :{}",textDetections[i].getDetectedText());
                this.timeCoords = textDetections[i].getPolygon();
                logger.info("左上坐标 X= {} Y= {}",getTimeLeftUpPoints().getX(),getTimeLeftUpPoints().getY());
                logger.info("右下坐标 X= {} Y= {}" ,getTimeRigthDownPoints().getX(),getTimeRigthDownPoints().getY());
                flag = false;
            }
            if(textDetections[i].getDetectedText().contains("删除")){
                logger.info("匹配到'删除'文字 :{}",textDetections[i].getDetectedText());
                if(!timeFlag && !textDetections[i].getDetectedText().contains("分钟") && !textDetections[i].getDetectedText().contains("小时")){
                    Matcher matcher_1 = pattern.matcher(textDetections[i-1].getDetectedText().trim());
                    if(matcher_1.find()){
                        this.deleteCoords = textDetections[i-1].getPolygon();
                        this.deleteCoords[1] = textDetections[i].getPolygon()[1];
                        this.deleteCoords[2] = textDetections[i].getPolygon()[2];
                        this.deleteCoords[3] = textDetections[i].getPolygon()[3];
                    }
                }else{
                    this.deleteCoords = textDetections[i].getPolygon();
                }
                logger.info("左上坐标 X= {} Y= {}",getDeleteLeftUpPoints().getX(),getDeleteLeftUpPoints().getY());
                logger.info("右下坐标 X= {} Y= {}" ,getDeleteRightDownPoints().getX(),getDeleteRightDownPoints().getY());
            }



        }
    }

    public TencentOcr(String imageBase64) throws TencentCloudSDKException {
        ocr(imageBase64);
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 09:30
     * @Description 获取"删除"左上坐标
     * @return com.tencentcloudapi.ocr.v20181119.models.Coord
     */
    public Coord getDeleteLeftUpPoints(){
        if(deleteCoords==null){
            throw new RuntimeException("为识别到删除按钮");
        }
        return getLeftUpPoints(deleteCoords);
    }
    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 09:30
     * @Description 获取"删除"右下坐标
     * @return com.tencentcloudapi.ocr.v20181119.models.Coord
     */
    public Coord getDeleteRightDownPoints(){
        if(deleteCoords==null){
            throw new RuntimeException("为识别到删除按钮");
        }
        return getRightDownPoints(deleteCoords);
    }

    public Coord getTimeLeftUpPoints(){
        if(timeCoords==null){
            throw new RuntimeException("为识别到系统时间");
        }
        return getLeftUpPoints(timeCoords);

    }

    public Coord getTimeRigthDownPoints(){
        if(timeCoords==null){
            throw new RuntimeException("为识别到系统时间");
        }
        return getRightDownPoints(timeCoords);

    }

    private Coord getLeftUpPoints(Coord[] coords){
        Coord coord = coords[0];
        for(int i = 1; i< coords.length; i++){
            if(coords[i].getX()<coord.getX() && coords[i].getY()<coord.getY()){
                coord = coords[i];
            }
        }
        return coord;
    }

    private Coord getRightDownPoints(Coord[] coords){
        Coord coord = coords[0];
        for(int i = 1; i< coords.length; i++){
            if(coords[i].getX()>coord.getX() && coords[i].getY()>coord.getY()){
                coord = coords[i];
            }
        }
        return coord;
    }
}

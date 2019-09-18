package com.nverrbug.wxjizan.jizan;

import com.nverrbug.wxjizan.WxJiZanApplication;
import com.nverrbug.wxjizan.constant.JiZanConstant;
import com.nverrbug.wxjizan.ocr.TencentOcr;
import com.nverrbug.wxjizan.utils.DateUtil;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Title: 微信集赞图片生成
 * Description: </p>
 * Email is Wenbo.Xie@b-and-qchina.com<
 * Company: http://www.bthome.com
 *
 * @author xie.wenbo
 * @date 2019-09-17 11:21
 */
public class WxJiZanImg {
    private static final Logger logger = LoggerFactory.getLogger(WxJiZanImg.class);
    /**
     * 图片分辨率
     */
    private Integer resolutionRatio_w,resolutionRatio_h;
    /**
     * 头像列数
     */
    private static int column_num = 9;
    /**
     * 图片清晰比率
     */
    private static float jPEGcompression = 0.75f;

    /**
     * 透明矩形宽度
     */
    private int alphaRectangWidth;

    /**
     * 边框距离
     */
    private static int praise_x = 31;

    /**
     * 头像宽、高
     */
    private int avatar_w,avatar_h;

    /**
     * 缝隙距离
     */
    private static int gap = 15;

    private static double comment_ratio = 0.134;

    private static double avatar_ratio = 0.0759;
    /**
     * 评论区大小
     */
    private int comment_w,comment_h;

    private void init(int resolutionRatio_h,int resolutionRatio_w){
        this.resolutionRatio_h = resolutionRatio_h;
        this.resolutionRatio_w = resolutionRatio_w;
        this.comment_w = resolutionRatio_w;
        this.comment_h = (int) (this.resolutionRatio_w.doubleValue()*comment_ratio);

        this.avatar_w = (int) (this.resolutionRatio_w.doubleValue()*avatar_ratio);
        this.avatar_h = avatar_w;
        this.alphaRectangWidth = resolutionRatio_w - praise_x*2;
    }

    /**
     * @Description : 图片文字合成
     * @Param : imageFile 图片
     * @Param : cityId 城市id
     * @Param : ana 语录
     * @Author : xie.wenbo
     * @Date : 2019/1/8
     */
    public ByteArrayOutputStream generateImg(InputStream inputStream, TencentOcr tencentOcr, int praiseNum, int bnqPraiseNum, String systemTime, String publishTime) throws IOException, TencentCloudSDKException {
        //读取原始位图
        BufferedImage imageLocal= ImageIO.read(inputStream);
        BufferedImage bufferedImage=new BufferedImage(imageLocal.getWidth(),imageLocal.getHeight(),BufferedImage.TYPE_INT_RGB);
        logger.info("图片的宽高为 {}X{}",bufferedImage.getHeight(),bufferedImage.getWidth());
        init(bufferedImage.getHeight(),bufferedImage.getWidth());
        Graphics2D g =bufferedImage.createGraphics();
        //将原始位图绘制到bufferedImage对象中
        g.drawImage(imageLocal,0,0,imageLocal.getWidth(),imageLocal.getHeight(),null);
        // 抗锯齿
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g.setComposite(ac);
        int deleteLeftUpY = tencentOcr.getDeleteLeftUpPoints().getY();
        //更改发布时间
        drawPublishTimeRegion(imageLocal,tencentOcr, publishTime, g, deleteLeftUpY);
        //更改系统时间
        drawSystemTimeRegion(imageLocal,tencentOcr,systemTime, g);
        //更改点赞区
        drawPraiseRegion(imageLocal,praiseNum, bnqPraiseNum, g, deleteLeftUpY);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        saveAsJPEG(bufferedImage, out);
        return out;
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 14:00
     * @Description 绘制更改发布时间区
     * @param imageLocal
     * @param tencentOcr
     * @param publishTime
     * @param g
     * @param deleteLeftUpY
     * @return void
     */
    private void drawPublishTimeRegion(BufferedImage imageLocal, TencentOcr tencentOcr, String publishTime, Graphics2D g, int deleteLeftUpY) {
        //更改删除行,更改发布时间、去掉仅自己可见
        g.setColor(new Color(imageLocal.getRGB(tencentOcr.getDeleteLeftUpPoints().getX(),tencentOcr.getDeleteLeftUpPoints().getY())));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
        g.fillRoundRect(tencentOcr.getDeleteLeftUpPoints().getX(),tencentOcr.getDeleteLeftUpPoints().getY(),
                tencentOcr.getDeleteRightDownPoints().getX()-tencentOcr.getDeleteLeftUpPoints().getX(),
                tencentOcr.getDeleteRightDownPoints().getY()-tencentOcr.getDeleteLeftUpPoints().getY(),0,0);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f));

        g.setFont( new Font(g.getFont().getName(), Font.PLAIN , tencentOcr.getDeleteRightDownPoints().getY()-tencentOcr.getDeleteLeftUpPoints().getY()));

        g.setColor(new Color (145, 148, 151));
        if(StringUtils.isEmpty(publishTime)){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MINUTE,new Random().nextInt(100)*-1);
            publishTime = DateUtil.dateToStr(calendar.getTime(),"HH:mm");
        }
        g.drawString(publishTime, tencentOcr.getDeleteLeftUpPoints().getX(), deleteLeftUpY+g.getFont().getSize());

        g.setColor(new Color (103, 115, 128));
        g.drawString("删除", tencentOcr.getDeleteLeftUpPoints().getX()+g.getFont().getSize()*3+g.getFont().getSize()/2, deleteLeftUpY+g.getFont().getSize());
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 13:57
     * @Description 绘制系统时间
     * @param imageLocal
     * @param tencentOcr
     * @param systemTime
     * @param g
     * @return void
     */
    private void drawSystemTimeRegion(BufferedImage imageLocal, TencentOcr tencentOcr, String systemTime, Graphics2D g) {
        //更改系统时间
        if(StringUtils.isEmpty(systemTime)){
            systemTime = DateUtil.dateToStr(new Date(),"HH:mm");
        }
        g.setColor(new Color (imageLocal.getRGB(tencentOcr.getTimeLeftUpPoints().getX(),tencentOcr.getTimeLeftUpPoints().getY())));
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
        g.fillRoundRect(tencentOcr.getTimeLeftUpPoints().getX(),tencentOcr.getTimeLeftUpPoints().getY(),
                tencentOcr.getTimeRigthDownPoints().getX()-tencentOcr.getTimeLeftUpPoints().getX(),
                tencentOcr.getTimeRigthDownPoints().getY()-tencentOcr.getTimeLeftUpPoints().getY(),0,0);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f));
        g.setColor(new Color (67, 67, 67));
        g.setFont( new Font(g.getFont().getName(), Font.PLAIN , tencentOcr.getTimeRigthDownPoints().getY()-tencentOcr.getTimeLeftUpPoints().getY()));
        g.drawString(systemTime, tencentOcr.getTimeLeftUpPoints().getX(), tencentOcr.getTimeRigthDownPoints().getY());
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 13:56
     * @Description 绘制点赞区
     * @param imageLocal
     * @param praiseNum
     * @param bnqPraiseNum
     * @param g
     * @param deleteLeftUpY
     * @return void
     */
    private void drawPraiseRegion(BufferedImage imageLocal, int praiseNum, int bnqPraiseNum, Graphics2D g, int deleteLeftUpY) throws IOException {
        //根据点赞数计算画布大小
        int line_num = praiseNum%column_num==0?praiseNum/column_num:praiseNum/column_num+1;
        //点赞区距离发布时间为 头像高+边框 的距离
        int praiseDistance = avatar_h+gap*2;
        int praise_y = deleteLeftUpY+praiseDistance;

        g.setColor(new Color(imageLocal.getRGB(praise_x,praise_y-20)));
        g.fillRoundRect(0,praise_y-20,resolutionRatio_w,resolutionRatio_h-praise_y,0,0);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f));

        g.setColor(new Color(237,237,237));
        g.fillRoundRect(praise_x,praise_y,alphaRectangWidth,line_num*(avatar_h+gap)+gap,0,0);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f));

        //绘制桃心
        BufferedImage heart_img = ImageIO.read(Objects.requireNonNull(WxJiZanApplication.class.getClassLoader().getResourceAsStream("heart.png")));
        g.drawImage(heart_img, praise_x+gap, praise_y+gap, avatar_w,avatar_h, null);

        //绘制小三角
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.fillPolygon(new int[]{praise_x+gap+8,praise_x+gap+avatar_w/2,praise_x+gap+avatar_w-12},new int[]{praise_y+22,praise_y-avatar_w/2+22,praise_y+22},3);


        int avatar_x=praise_x+avatar_w+gap;
        int avatar_y=praise_y+gap;

        List<String> avatarImgs = initAvatarImgs(praiseNum,bnqPraiseNum);
        for(int i = 0;i<avatarImgs.size();i++){
            BufferedImage avatar_img = ImageIO.read(Objects.requireNonNull(WxJiZanApplication.class.getClassLoader().getResourceAsStream(avatarImgs.get(i))));
            g.drawImage(setRadius(avatar_img,80,0,0), avatar_x+(((i%column_num)*gap))+avatar_w*(i%column_num), avatar_y+(i/column_num)*gap+(i/column_num)*avatar_h, avatar_w,avatar_h, null);
        }

        //绘制评论框
        BufferedImage comment_img = ImageIO.read(Objects.requireNonNull(WxJiZanApplication.class.getClassLoader().getResourceAsStream("comment.png")));
        g.drawImage(comment_img, 0, resolutionRatio_h-comment_h, comment_w,comment_h, null);
    }

    /**
     * 以JPEG编码保存图片
     *
     * @param imageToSave
     *            要处理的图像图片
     * @param fos
     *            文件输出流
     * @throws IOException
     */
    private static void saveAsJPEG(BufferedImage imageToSave, ByteArrayOutputStream fos) throws IOException {
        ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(fos);
        imageWriter.setOutput(ios);
        if (jPEGcompression >= 0 && jPEGcompression <= 1f) {
            // new Compression
            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(jPEGcompression);

        }
        // new Write and clean up
        ImageIO.setUseCache(false);
        imageWriter.write(new IIOImage(imageToSave, null, null));
        ios.close();
        imageWriter.dispose();
    }

    /**
     * 图片设置圆角
     * @param srcImage
     * @param radius
     * @param border
     * @param padding
     * @return
     * @throws IOException
     */
    public static BufferedImage setRadius(BufferedImage srcImage, int radius, int border, int padding) throws IOException{
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        int canvasWidth = width + padding * 2;
        int canvasHeight = height + padding * 2;

        BufferedImage image = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();
        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setColor(Color.WHITE);
        gs.fill(new RoundRectangle2D.Float(0, 0, canvasWidth, canvasHeight, radius, radius));
        gs.setComposite(AlphaComposite.SrcAtop);
        gs.drawImage(setClip(srcImage, radius), padding, padding, null);
        if(border !=0){
            // 0.3f为透明度 ，值从0-1.0，依次变得不透明
            gs.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
            gs.setStroke(new BasicStroke(border));
            gs.drawRoundRect(padding, padding, canvasWidth - 2 * padding, canvasHeight - 2 * padding, radius, radius);
        }
        gs.dispose();
        return image;
    }

    /**
     * 图片切圆角
     * @param srcImage
     * @param radius
     * @return
     */
    public static BufferedImage setClip(BufferedImage srcImage, int radius){
        int width = srcImage.getWidth();
        int height = srcImage.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gs = image.createGraphics();

        gs.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gs.setClip(new RoundRectangle2D.Double(0, 0, width, height, radius, radius));
        gs.drawImage(srcImage, 0, 0, null);
        gs.dispose();
        return image;
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 16:32
     * @Description 初始化头像集合
     * @param count 总数
     * @param bnqImg 百安居头像数量
     * @return java.util.List<java.lang.String>
     */
    public static List<String> initAvatarImgs(Integer count, Integer bnqImg){
        List<String> avatarImgs = new ArrayList<>();
        List<String> allAvatarImgs = initAvatarImgs();
        for(int i =0;i<bnqImg;i++){
            avatarImgs.add("avatars/avatar_1.jpg");
        }
        for(int i =0;i<count-bnqImg;i++){
            avatarImgs.add(allAvatarImgs.get(i));
        }
        Collections.shuffle(avatarImgs);
        return avatarImgs;
    }
    /**
     * @author xie.wenbo
     * @date Created on 2019/9/17 16:38
     * @Description 初始化头像集合 随机排序
     * @return java.util.List<java.lang.String>
     */
    public static List<String> initAvatarImgs(){
        List<String> avatarImgs = new ArrayList<>();
        for(int i=1;i<JiZanConstant.avatarNums;i++){
            avatarImgs.add("avatars/avatar_"+i+".jpg");
        }
        Collections.shuffle(avatarImgs);
        return avatarImgs;
    }


}

package amilalaflower.business;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.springframework.beans.factory.annotation.Autowired;

import amilalaflower.common.DGConst;
import amilalaflower.common.DataGetterException;
import amilalaflower.common.HtmlParser;
import amilalaflower.common.MachineData;
import amilalaflower.common.Property;
import amilalaflower.mbg.entity.MMachineList;
import amilalaflower.mbg.entity.MMachineListExample;
import amilalaflower.mbg.entity.TMachineData;
import amilalaflower.mbg.mapper.MMachineListMapper;
import amilalaflower.mbg.mapper.TMachineDataMapper;

public class DataGetterBusiness {

    /**
     * Log4j
     */
    private Logger log = LogManager.getLogger();

    /*
     * machineData
     */
    private MachineData md = new MachineData();

    /**
     * Mapper
     */
     @Autowired
      private TMachineDataMapper mdmapper;
     @Autowired
     private MMachineListMapper mlmapper;

     /**
      * Property
      */
     @Autowired
     private Property prop;

     /**
      * ホールID
      */
     private String hallId;

     /**
      * トータル差枚
      */
     private int totalGames;

     /**
      * トータルゲーム数
      */
     private int totalSamai;

     // ホールURL
     private String hallurl;
     // スロットディレクトリ
     private String slotDir;
     // パチンコディレクトリ
     private String pachiDir;
     // スリープ時間
     private int sleeptime;

    /**
     * メイン処理実行
     * @throws Exception 例外
     */
    public void execute(final String[]args) throws Exception {
        // 変数初期化
        totalGames = 0;
        totalSamai = 0;

        // ホールID
        hallId = args[0];

        // 設定値取得
        hallurl = prop.getHall_url();
        slotDir = prop.getSlot_dir();
        pachiDir = prop.getPachi_dir();
        sleeptime = Integer.parseInt(prop.getSleeptime());

        try{
            // 必須ディレクトリ作成処理呼び出し処理
            createDir(slotDir);
            createDir(pachiDir);

            // 日付取得(フォーマット:yyyy-mm-dd)
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date strDate = sdf.parse(sdf.format(cal.getTime()));

            //日付のセット
            md.setDate(strDate);

            // 台リスト取得
            MMachineListExample where = new MMachineListExample();
            where.createCriteria()
                .andHallIdEqualTo(hallId);
            where.setOrderByClause("MACHINE_ID");
            List<MMachineList> machineList = mlmapper.selectByExample(where);

            // 台データ登録処理
            List<String> numberList = machineList.stream()
                .sequential()
                .map(MMachineList::getMachineId)
                .map(Object::toString)
                .collect(Collectors.toList());

            for (final String number : numberList) {
                getMachineData(number);
            }

            // 総計をログに出力
            log.info("トータル差枚:{}  トータルゲーム数:{}", totalSamai, totalGames);

        } catch (DataGetterException e) {
            throw e;
        } catch (Exception e) {
            log.error("予期せぬエラーが発生しました", e);
            throw e;
        }
    }

    /**
     * 差枚計算
     * @throws Exception 例外
     */
    private void getMachineData(final String number) throws Exception {
        try {
            log.debug("MachineNumber:{}", number);
            StringBuilder buf = new StringBuilder();
            String graphUrl = null;
            HtmlParser htmlparse = null;
            HtmlCleaner cleaner = null;

            // 台データ格納ディレクトリ作成処理
            buf.append(slotDir);
            buf.append(number);
            createDir(buf.toString());
            buf.setLength(0);

            // ゴミファイル削除処理
            buf.append(slotDir);
            buf.append(number);
            buf.append(DGConst.HTML_FILENAME);
            String machineHtml = buf.toString();
            buf.setLength(0);
            deleteFile(machineHtml);
            buf.append(slotDir);
            buf.append(number);
            buf.append(DGConst.HTML_FILENAME);
            String machineGraphPath = buf.toString();
            buf.setLength(0);
            deleteFile(machineGraphPath);

            // 台データのHTMLページダウンロード処理
            buf.append(hallurl);
            buf.append(number);
            String machineUrl = buf.toString();
            buf.setLength(0);
            download(machineUrl, machineHtml);

            cleaner = new HtmlCleaner();

            // HTMLノード取得
            TagNode node = cleaner.clean(new File(machineHtml), DGConst.ENC_UTF_8);
            htmlparse = new HtmlParser(node);
            graphUrl = htmlparse.getGraph(number);

            // グラフ画像ダウンロード処理
            download(graphUrl, machineGraphPath);

            md.setMachineNo(number);
            md.setMachineName(htmlparse.getName(number));
            md.setSamai(readGraph(machineGraphPath));
            md.setGames(htmlparse.getGames(number));

            log.info("台番号:{} 機種名:{} ゲーム数:{} 差枚:{}",
                    md.getMachineNo(), md.getMachineName(), md.getGames(), md.getSamai());

            insertMachineData(md);

            totalGames = totalGames + md.getGames();
            totalSamai = totalSamai + md.getSamai();

            Thread.sleep(sleeptime);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * ディレクトリ作成処理
     * @param dirPath ディレクトリパス
     * @throws DataGetterException 例外
     */
    private void createDir(final String dirPath) throws DataGetterException {

        try {
            File dir = new File(dirPath);
            if(!dir.exists()) {
                FileUtils.forceMkdir(dir);
            }
        } catch (IOException e) {
            log.error("ファイル作成処理失敗 ディレクトリパス:{}", dirPath);
            throw new DataGetterException();
        }
    }

    /**
     * ファイル削除処理
     * @param dirPath ディレクトリパス
     * @throws DataGetterException 例外
     */
    private void deleteFile(final String dirPath) throws DataGetterException {

        File gomi = new File(dirPath);
        try {
            if(gomi.exists()){
                FileUtils.forceDelete(gomi);
            }
        } catch (IOException e) {
            log.error("ファイル削除処理失敗 ディレクトリパス:{}", dirPath);
            throw new DataGetterException();
        }
    }

    /**
     * ファイルダウンロード処理
     * @param htmlURL ダウンロードURL
     * @param saveFilePath 保存先
     * @throws DataGetterException 例外
     */
    private void download(final String htmlURL, final String saveFilePath) throws DataGetterException {
        // 保存先指定
        File saveFile = new File(saveFilePath);
        try {
            URL url = new URL(htmlURL);
            URLConnection con = url.openConnection();

            try (InputStream in = con.getInputStream();
                 FileOutputStream out = new FileOutputStream(saveFile, false);) {

                byte[] b = new byte[4096];
                int readByte = 0;

                while(-1 != (readByte = in.read(b))) {
                    out.write(b, 0, readByte);
                }
            }
        } catch (Exception e) {
            log.error("ファイルのダウンロードに失敗しました", e);
            throw new DataGetterException(e);
        }
    }

    /**
     * グラフ画像を読み取り座標を取得
     * @param graphPicPath スランプグラフファイルパス
     * @throws DataGetterException 例外
     * @return samai 差枚
     */
    private int readGraph(final String graphPicPath) throws DataGetterException {

        BufferedImage readGraph = null;
        int samai = 0;

        // プロパティファイル値取得
        // 高さチェック値
        int heightchk = Integer.parseInt(prop.getHeight());
        // 幅チェック値
        int widthachk = Integer.parseInt(prop.getWidth());
        // 閉店座標
        int closedPx = Integer.parseInt(prop.getClosed_px());
        // ターゲット色
        int targetColor = Integer.parseInt(prop.getGraph_color());

        try{
            int color = 0;

            File graphPic = new File(graphPicPath);
            readGraph = ImageIO.read(graphPic);

            int height = readGraph.getHeight();
            int width = readGraph.getWidth();
            // 画像サイズが正しいかチェック
            if(heightchk != height || widthachk != width) {
                throw new IOException("ファイルの形式が不正です");
            }

            outside:
            for(int x = closedPx;x > 0 ;x--) {
                for(int y = 0;y < height;y++) {
                    color = readGraph.getRGB(x,y);
                    log.trace("X:{} Y:{} getRGB:{}", x, y , color);
                    // 指定した座標の色を取得
                    if(targetColor == color) {
                        samai = getSamai(y + 2);
                        break outside;
                    }
                }
            }

            return samai;

        }catch(IOException e){
            // 読み込みもしくはデコードエラー
            log.error("スランプグラフデータ読み込み失敗", e);
            throw new DataGetterException();
        }
    }

    /**
     * 取得した座標から差枚を計算
     * @param y 取得座標
     * @return saimai 差枚
     */
    private int getSamai(final int y) {
        int samai;
        double pixel;

        // 設定ファイル値取得
        // 差枚0Y座標
        int zeroPx = Integer.parseInt(prop.getZero_px());
        // 1PXあたりのメダル
        double medalOnePx = Double.parseDouble(prop.getMedal_px());

        pixel = y - zeroPx;
        log.debug("Pixel:{}", pixel);
        samai = (int)(pixel * medalOnePx);
        samai = -samai;
        log.debug("PayOut:{}", samai);
        return samai;
    }

    /**
     * 台データ登録処理
     * @param MachineData 台データ
     * @throws DataGetterException 例外
     */
    private void insertMachineData(final MachineData md) throws DataGetterException{
        try {
            TMachineData insertMd = new TMachineData();
            insertMd.setHallId(hallId);
            insertMd.setAcquisitionDate(md.getDate());
            insertMd.setMachineNo(md.getMachineNo());
            insertMd.setMachineName(md.getMachineName());
            insertMd.setGames(md.getGames());
            insertMd.setPayout(md.getSamai());
            mdmapper.insert(insertMd);
        } catch (Exception e) {
            log.error("台データ登録失敗 -台データ取得日:{} -台番号:{}", md.getDate(), md.getMachineNo(), e);
            throw new DataGetterException();
        }
    }
}

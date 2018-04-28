import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Modle {
    private int id;
    private String name;

    public Modle(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


public class Main {

    // 地址

    // 获取img标签正则
    private static final String IMGURL_REG = "\"pic_url\":\".*?\\.jpg\"";
    private static final String IMGURL = "\\<img width=\"2.*\" height=\"2.*\".*\\.jpg";
    // 获取src路径的正则
   // private static final String IMGSRC_REG = "[a-zA-z]+://[^\\s]*";

    private static ArrayList<Integer> errorID = new ArrayList<>();

    private int  ok[];

    //private int errorid1[] = {3,6,17,25,27,29,47,52,53,69,74,81,85,88,91,94,95,99,100,109,115,126,133,137,148,149,151,153,158,160,171,174,180,183,193,197,198,199,202,204,205,206,211,212,213,214,225,226,229,232,233,238,260,263,264,266,273,274,282,285,288,299,306,310,314,316,328,330,332,335,336,337,340,343,344,360,362,368,373};
    //private int errorid2[] = {85,95,148,160,183,193,206,211,233,314,330,362,368};
    private int errorid3[] = {285,286,287,288,289,290,291,292,293,294,295,296};

    public ArrayList<Modle> getBooksName() throws Exception {

        String URL = "jdbc:mysql://localhost:3306/BLS";
        String USER = "root";
        String PASS = "123456";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT id,title FROM bookinfos";

        conn = DriverManager.getConnection(URL,USER,PASS);
        pstmt = conn.prepareStatement(sql);
        rs = pstmt.executeQuery();

        ArrayList<Modle> res = new ArrayList<>();

        while ( rs.next() ) {
                Modle tmp = new Modle(rs.getInt(1),
                        rs.getString(2));
                res.add(tmp);
        }

        rs.close();
        pstmt.close();
        conn.close();

        return res;

    }

    public Main() {
        ok = new int[500];
        for(int i=0; i<500; i++ ) {
            ok[i] = 0;
        }
    }


    public static void main(String[] args) {
        Main cm=new Main();

        try {
            ArrayList<Modle> nameList = cm.getBooksName();
            for( int j=0; j<nameList.size(); j++ ){

                String searchName = nameList.get(j).getName().replaceAll(" ","");

                String URL = "https://search.jd.com/Search?keyword="+searchName+"&enc=utf-8&wq="+searchName;

                String HTML = cm.getHtml(URL,nameList.get(j).getId());

                //获取图片标签
                List<String> imgUrl = cm.getImageUrl(HTML);


                for( int i=0; i<imgUrl.size(); i++ ) {
                    String[] str = imgUrl.get(i).split("\\//");
                    imgUrl.set(i,"https://"+str[1]);
                }

                //下载图片
               cm.Download2(imgUrl,nameList.get(j));

                System.out.println("完成度:"+(((double)(j+1))/nameList.size())*100f+"%");

            }
            //获得html文本内容


        }catch (Exception e){
            e.printStackTrace();
        }

        cm.logging();

    }


    public void logging() {
        System.out.println("\nError ID :");
        for (int i=0; i<errorID.size(); i++ )
            System.out.print(errorID.get(i)+",");
        System.out.println("\n");
        System.out.println("Not true:");
        for (int i = 1; i <= 404; i++) {
            if( ok[i] != 1 ){
                System.out.print(i+",");
            }
        }
    }

    //获取HTML内容
    private String getHtml(String url, int id){

        StringBuffer sb=new StringBuffer("");
        try{
            URL url1=new URL(url);
            URLConnection connection=url1.openConnection();

            InputStream in=connection.getInputStream();
            InputStreamReader isr=new InputStreamReader(in);
            BufferedReader br=new BufferedReader(isr);

            String line;
            while((line=br.readLine())!=null){
                sb.append(line,0,line.length());
                sb.append('\n');
            }
            br.close();
            isr.close();
            in.close();

        } catch ( IOException ex ) {
            ex.printStackTrace();
            errorID.add(id);
        }

        return sb.toString();
    }

    //获取ImageUrl地址
    private List<String> getImageUrl(String html){
        Matcher matcher=Pattern.compile(IMGURL).matcher(html);
        List<String>listimgurl=new ArrayList<String>();
        while (matcher.find()){
            listimgurl.add(matcher.group());
        }

        return listimgurl;
    }

//    //获取ImageSrc地址
//    private List<String> getImageSrc(List<String> listimageurl){
//        List<String> listImageSrc=new ArrayList<String>();
//        for (String image:listimageurl){
//            Matcher matcher=Pattern.compile(IMGSRC_REG).matcher(image);
//            while (matcher.find()){
//                listImageSrc.add(matcher.group().substring(0, matcher.group().length()-1));
//            }
//        }
//        return listImageSrc;
//    }

    private boolean Download2(List<String> listImgSrc, Modle tmp) {
        int cunt = 1;
        for(String url: listImgSrc) {
            try {
                URL uri = new URL(url);
                InputStream is = uri.openStream();
                FileOutputStream fos = new FileOutputStream(new File("/home/yms/Documents/ProjectImg/" + tmp.getId() + "-" + cunt + ".jpg"));
                byte[] bbuf = new byte[1024];
                int length = 0;
                System.out.print("Start Download "+tmp.getName()+":"+tmp.getId()+" "+ url);
                while ((length = is.read(bbuf, 0, bbuf.length)) != -1) {
                    fos.write(bbuf, 0, length);
                }

                fos.close();
                is.close();

                System.out.println("Download Ok");
                ok[tmp.getId()] = 1;
                cunt++;
                if (cunt > 2) {
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
                errorID.add(tmp.getId());
            }
        }

        return true;
    }

    //下载图片
    private boolean Download(List<String> listImgSrc, Modle tmp) {
        try {
            //开始时间
            Date begindate = new Date();
            Integer cnt = 1;
            for (String url : listImgSrc) {
                //开始时间
                Date begindate2 = new Date();
               // String imageName = url.substring(url.lastIndexOf("/") + 1, url.length());
                URL uri = new URL(url);
                InputStream in = uri.openStream();
                FileOutputStream fo = new FileOutputStream(new File("/home/yms/文档/java/JavaPC/DownLoadIMG/"+tmp.getId()+"-"+cnt.toString()+".jpg"));
                byte[] buf = new byte[1024];
                int length = 0;
                System.out.println("开始下载:" + url);
                while ((length = in.read(buf, 0, buf.length)) != -1) {
                    fo.write(buf, 0, length);
                }
                in.close();
                fo.close();
                System.out.println(tmp.getName() + "下载完成");

                return true;

//                return true;
//                //结束时间
//                Date overdate2 = new Date();
//                double time = overdate2.getTime() - begindate2.getTime();
//                System.out.println("耗时：" + time / 1000 + "s");
//
//                cnt++;
            }
            Date overdate = new Date();
            double time = overdate.getTime() - begindate.getTime();
            System.out.println("总耗时：" + time / 1000 + "s");
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }
}
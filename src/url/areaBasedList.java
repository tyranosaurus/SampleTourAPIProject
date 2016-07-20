package url;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

// 지역기반 관광정보 조회
// 지역 및 시군구를 기반으로 관광정보 목록을 조회하는 기능입니다.
// 파라미터에 따라 제목순, 수정일순(최신순), 등록일순, 인기순 정렬검색을 제공합니다.

public class areaBasedList {
	private static String AREACODE = "1";				// 지역코드
	private static String SIGUNGUCODE = "";			// 시군구코드
	
	public static void main(String[] args) throws Exception {
		 	StringBuilder urlBuilder = new StringBuilder("http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList"); /*URL*/
	        urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8")+ "=FYPMcB84UK5jn7XCnYqm1lJMqpuKakFgS4k3TII0xsef95IkojvpIVNDfBV6QQxm%2B9Dm9c7O%2FcIha6glL%2FVrEw%3D%3D"); /*Service Key*/
	        
	        urlBuilder.append("&" + URLEncoder.encode("areaCode","UTF-8") + "=" + URLEncoder.encode(AREACODE, "UTF-8")); /*지역코드*/
	        //urlBuilder.append("&" + URLEncoder.encode("sigunguCode","UTF-8") + "=" + URLEncoder.encode(SIGUNGUCODE, "UTF-8")); /*시군구코드*/
	        
	        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*검색건수*/
	        urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
	       
	        urlBuilder.append("&" + URLEncoder.encode("MobileOS","UTF-8") + "=" + URLEncoder.encode("ETC", "UTF-8")); /*검색건수*/
	        urlBuilder.append("&" + URLEncoder.encode("MobileApp","UTF-8") + "=" + URLEncoder.encode("TravelFriend", "UTF-8")); /*페이지 번호*/
	        
	        URL url = new URL(urlBuilder.toString());
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-type", "application/json");
	        System.out.println("Response code: " + conn.getResponseCode());
	        BufferedReader rd;
	        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
	            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        } else {
	            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
	        }
	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = rd.readLine()) != null) {
	            sb.append(line);
	        }
	        rd.close();
	        conn.disconnect();
	        System.out.println(sb.toString());
	}

}

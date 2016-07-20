package XMLParser;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/*
 * TOUR API - XPath Parsing
 * 1. 지역기반 관광조회 - contentId 얻음
 * 2. 앞에서 얻은 contentId로 상세정보 조회
 */

public class XPathParsing {
	private static String AREACODE = "1";			// 지역코드
	private static String SIGUNGUCODE = "";			// 시군구코드
	private static String NUMOFROWS = "1";			// 한 페이지 결과 수
	private static String PAGENO = "1";				// 페이지번호
	
	private static String CITYLIST_NO = "1";			// DB넣을 때 사용
	private static ArrayList<String> contIdList =  new ArrayList<String>();		// contentId List
	
	public static void main(String[] args) throws Exception {
	
		xmlDefault();
	
	}
	private static void dbInsert(ArrayList<String> list) {		// DB에 넣는 작업
		StringBuilder urlBuilder = new StringBuilder("http://115.68.116.235/aradongbros/???.php"); /* URL */ 

		try {
			urlBuilder.append("?" + URLEncoder.encode("cityList_no", "UTF-8") + "=" + URLEncoder.encode(CITYLIST_NO, "UTF-8")); /* cityList_no */
			urlBuilder.append("&" + URLEncoder.encode("postList_name", "UTF-8") + "=" + URLEncoder.encode(list.get(4), "UTF-8")); /* 제목 */
			urlBuilder.append("&" + URLEncoder.encode("postList_location", "UTF-8") + "=" + URLEncoder.encode(list.get(3), "UTF-8")); /* GPS X,Y좌표 */
			urlBuilder.append("&" + URLEncoder.encode("postList_picture", "UTF-8") + "=" + URLEncoder.encode(list.get(1), "UTF-8")); /* 대표이미지 */
			urlBuilder.append("&" + URLEncoder.encode("postList_info", "UTF-8") + "=" + URLEncoder.encode(list.get(5), "UTF-8")); /* 개요 */
			urlBuilder.append("&" + URLEncoder.encode("postList_category", "UTF-8") + "=" + URLEncoder.encode(list.get(0), "UTF-8")); /* 컨텐츠타입 */
			
			
			URL url = new URL(urlBuilder.toString());
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	        conn.setRequestMethod("GET");
	        conn.setRequestProperty("Content-type", "application/json");
	        System.out.println("Response code: " + conn.getResponseCode());
			
			//System.out.println("url:"+url);
	        conn.disconnect();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private static void xmlDefault() throws Exception {	
		
		String url = URLContIdBuilder() + "";		// 1. contentid 얻기
		xmlContIdParsing(url);						// xml 파싱
		
		// -----------------------------------------
		// 2. contentid 이용해서 상세정보 얻기
		for(int i=0;i<contIdList.size(); i++){
			String res = URLDetailBuilder(contIdList.get(i))+"";	
			System.out.println("결과: "+res);

			xmlDetailParsing(res);	// 파싱하기
		}
		
	}

	private static void xmlDetailParsing(String url)	throws Exception {

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url);			// XML Document 객체 생성
		XPath xpath = XPathFactory.newInstance().newXPath();												// xpath 생성

		NodeList item = (NodeList) xpath.evaluate("//*/item", document, XPathConstants.NODESET);			// 모든 item 값을 가져오기
		ArrayList<String> list = new ArrayList<String>();
		
		System.out.println("item.getLength():"+item.getLength());
		if (item.getLength() == 0) {
			System.out.println(" 조회 결과가 없습니다. ");
		}

		for (int idx = 0; idx < item.getLength(); idx++) {
			list.clear(); // 초기화

			NodeList nodeList = item.item(idx).getChildNodes();		//item의 자식들(11개노드)
						        
	        int contenttypeid =  (int) xpath.evaluate("contenttypeid", nodeList, XPathConstants.NUMBER);/*컨텐츠타입ID*/
	        String firstimage = (String) xpath.evaluate("firstimage", nodeList, XPathConstants.STRING);/*대표이미지*/
	        String addr1 = (String) xpath.evaluate("addr1", nodeList, XPathConstants.STRING);/*주소*/
	        String mapx = (String) xpath.evaluate("mapx", nodeList, XPathConstants.STRING);/*GPS X좌표*/
	        String mapy = (String) xpath.evaluate("mapy", nodeList, XPathConstants.STRING);/*GPS Y좌표*/
	        String title = (String) xpath.evaluate("title", nodeList, XPathConstants.STRING);/*제목*/
	        String overview = (String) xpath.evaluate("overview", nodeList, XPathConstants.STRING);/*개요*/
	        
	        if( contenttypeid == 32 ){
	        	list.add("inn");
	        }else if( contenttypeid == 39 ){
	        	list.add("food");
	        }else{
	        	list.add("tour");
	        }	
	        list.add(firstimage);
	        list.add(addr1);
	        list.add(mapx+","+mapy);
	        list.add(title);
	        list.add(overview);
	        
	        // DB Insert
	        dbInsert(list);	// db에 넣는 작업
		}//for_end
		
	}
	
	
	private static void xmlContIdParsing(String url)	throws Exception {

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url);			// XML Document 객체 생성
		XPath xpath = XPathFactory.newInstance().newXPath();												// xpath 생성

		NodeList item = (NodeList) xpath.evaluate("//*/item/contentid", document, XPathConstants.NODESET);	// item밑의 contentid값만 가져오기
		
		System.out.println("item.getLength():"+item.getLength());
		if (item.getLength() == 0) {
			System.out.println(" 조회 결과가 없습니다. ");
		}
		
		for (int idx = 0; idx < item.getLength(); idx++) {
			contIdList.add(item.item(idx).getTextContent());			
		}
		

		
	}

	public static URL URLDetailBuilder(String contID) { 		// URL 만드는 작업 - detailCommon(상세정보)
		StringBuilder urlBuilder = new StringBuilder("http://api.visitkorea.or.kr/openapi/service/rest/KorService/detailCommon"); /*URL*/

		try {
			 urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8")+ "=FYPMcB84UK5jn7XCnYqm1lJMqpuKakFgS4k3TII0xsef95IkojvpIVNDfBV6QQxm%2B9Dm9c7O%2FcIha6glL%2FVrEw%3D%3D"); /*Service Key*/
			 urlBuilder.append("&" + URLEncoder.encode("contentId","UTF-8") + "=" + URLEncoder.encode(contID, "UTF-8")); /*컨텐츠ID*/
			 urlBuilder.append("&" + URLEncoder.encode("defaultYN","UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*기본정보 조회*/
			 urlBuilder.append("&" + URLEncoder.encode("firstImageYN","UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*대표이미지 조회*/
			 urlBuilder.append("&" + URLEncoder.encode("addrinfoYN","UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*주소정보 조회*/
			 urlBuilder.append("&" + URLEncoder.encode("mapinfoYN","UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*좌표정보 조회*/
			 urlBuilder.append("&" + URLEncoder.encode("overviewYN","UTF-8") + "=" + URLEncoder.encode("Y", "UTF-8")); /*컨텐츠 개요 조회*/
			 
		     urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*검색건수*/
		     urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
		     urlBuilder.append("&" + URLEncoder.encode("MobileOS","UTF-8") + "=" + URLEncoder.encode("ETC", "UTF-8")); /*MobileOS*/
		     urlBuilder.append("&" + URLEncoder.encode("MobileApp","UTF-8") + "=" + URLEncoder.encode("TravelFriend", "UTF-8")); /*MobileApp*/

			URL url = new URL(urlBuilder.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-type", "application/json");
			System.out.println("Response code: " + conn.getResponseCode());

			return url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static URL URLContIdBuilder() { 		// URL 만드는 작업 - contentid 
		StringBuilder urlBuilder = new StringBuilder("http://api.visitkorea.or.kr/openapi/service/rest/KorService/areaBasedList"); /*URL*/

		try {
			 urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8")+ "=FYPMcB84UK5jn7XCnYqm1lJMqpuKakFgS4k3TII0xsef95IkojvpIVNDfBV6QQxm%2B9Dm9c7O%2FcIha6glL%2FVrEw%3D%3D"); /*Service Key*/
			 urlBuilder.append("&" + URLEncoder.encode("areaCode","UTF-8") + "=" + URLEncoder.encode(AREACODE, "UTF-8")); /*지역코드*/
		     //urlBuilder.append("&" + URLEncoder.encode("sigunguCode","UTF-8") + "=" + URLEncoder.encode(SIGUNGUCODE, "UTF-8")); /*시군구코드*/
		        
		     urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode(NUMOFROWS, "UTF-8")); /*검색건수*/
		     urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지 번호*/
		       
		     urlBuilder.append("&" + URLEncoder.encode("MobileOS","UTF-8") + "=" + URLEncoder.encode("ETC", "UTF-8")); /*MobileOS*/
		     urlBuilder.append("&" + URLEncoder.encode("MobileApp","UTF-8") + "=" + URLEncoder.encode("TravelFriend", "UTF-8")); /*MobileApp*/

			URL url = new URL(urlBuilder.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-type", "application/json");
			System.out.println("Response code: " + conn.getResponseCode());

			return url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

/*
<item>
				<areacode>1</areacode>
				<cat1>C01</cat1>
				<cat2>C0115</cat2>
				<cat3>C01150001</cat3>
				<contentid>2044565</contentid>
				<contenttypeid>25</contenttypeid>
				<createdtime>20151201164850</createdtime>
				<firstimage>http://tong.visitkorea.or.kr/cms/resource/57/1808157_image2_1.jpg</firstimage>
				<firstimage2>http://tong.visitkorea.or.kr/cms/resource/57/1808157_image3_1.jpg</firstimage2>
				<mapx>126.9732511193</mapx>
				<mapy>37.5013561691</mapy>
				<mlevel>6</mlevel>
				<modifiedtime>20151230131632</modifiedtime>
				<readcount>584</readcount>
				<sigungucode>12</sigungucode>
				<title>‘충혼’의 수양벚꽃 마중하는 호젓한 꽃길</title>
			</item>
			<item>
				<addr1>서울특별시 강서구 화곡4동 799-9번지</addr1>
				<addr2>(화곡동)</addr2>
				<areacode>1</areacode>
				<cat1>B02</cat1>
				<cat2>B0201</cat2>
				<cat3>B02010900</cat3>
				<contentid>1747824</contentid>
				<contenttypeid>32</contenttypeid>
				<createdtime>20121105144638</createdtime>
				<firstimage>http://tong.visitkorea.or.kr/cms/resource/22/1744722_image2_1.jpg</firstimage>
				<firstimage2>http://tong.visitkorea.or.kr/cms/resource/22/1744722_image3_1.jpg</firstimage2>
				<modifiedtime>20160510175749</modifiedtime>
				<readcount>14505</readcount>
				<sigungucode>4</sigungucode>
				<tel>02-2643-8800</tel>
				<title>㈜코스테이</title>
				<zipcode>157-903</zipcode>
			</item>
 */


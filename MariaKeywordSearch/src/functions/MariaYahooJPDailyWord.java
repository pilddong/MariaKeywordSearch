package functions;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.File;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;   

public class MariaYahooJPDailyWord { 
    public static void main(String[] args) throws Exception {
    	
    	ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setDebugEnabled(true)
    	  .setOAuthConsumerKey("o7oKG8OmnlErJ95d5gsH4PDPZ")
    	  .setOAuthConsumerSecret("L2Q2HukCBoRVWo25x51whjCbCkEgjaSKPz6UzFMStrSFXlZpLn")
    	  .setOAuthAccessToken("597051361-jlLoTnMN6SfWsqIJdglQgWmimk6TWflaTLTwHR8C")
    	  .setOAuthAccessTokenSecret("m2OsUtmnJ7KhihUuxGeRfEZSimmFPCytSQABq1K0SOsbs");
    	TwitterFactory tf = new TwitterFactory(cb.build());
    	Twitter twitter = tf.getInstance();
    	
    	
    	
        
    	String pageUrl="http://searchranking.yahoo.co.jp/rss/burst_ranking-rss.xml"; 

        Document doc = Jsoup.connect( pageUrl ).get(); 
  
        String selector="channel > item > title"; // css selector

        Elements rcw = doc.select( selector ); 

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
        long nowmills = System.currentTimeMillis(); 
        String now = sdf.format(new Date(nowmills)); 
        System.out.println("실검색어 가져온 시간 : "+now); 
        ArrayList<String> searchlist = new ArrayList<String>();
        
        // 검색어 추출
        for (Element el : rcw) { 
        	System.out.println(el);
            String title =el.text(); 
 
            searchlist.add(title);

            System.out.println(title);
            
        } 
        
       
        // 검색어 추출 끝
        
        System.out.println(searchlist);
        
        ArrayList<WordCount> words;
        
        // 검색아이템 하나씩 모두 for문
        for(String searchItem : searchlist)
        try {
        	String str = "";
       	 	Query query = new Query(searchItem);
            query.setCount(200);	            
            //query.setLang("ko");

            int repeat = 3;

            QueryResult result;

   	         result = twitter.search(query);
   	         
   	         List<Status> tweets = result.getTweets();
   	         
   	         for(int i = 0; i < repeat; i++) {
   	      
   		         if((query = result.nextQuery()) == null) continue;
   		         result = twitter.search(query);

   		         tweets.addAll(result.getTweets());

   	         }

            for (int i = 0; i < tweets.size(); i++) {
   	         String s = new String(tweets.get(i).getText());
   	    		
   	    		if(filter19(s)) {
   	    			tweets.remove(i--);
   	    			continue;
   	    		}

   	    		
   	    		if(tweets.get(i).getURLEntities().length != 0) {
   	    			tweets.remove(i--);
   	    			continue;
        		}
        		
        		
        		System.out.println(s);
            }
            
            for (Status tweet : tweets) {
           	 
            	if(tweet.isRetweet() == true) continue;

           		if(tweet.getURLEntities().length != 0) {

           			continue;
           		}

            	str+=tweet.getText() + "\n";

            }
            
            Filtering filtering = new Filtering();
            
            Scanner scan = new Scanner(str);
            HashMap<String, Integer> count = new HashMap<String, Integer>();
            // 단어 카운트 하기
            while (scan.hasNext()) {
            	
                String word = removePunctuations(scan.next());
                word = remove_comma(word);
                word = remove_broadcast(word);
                if (Filtering.filteringList().contains(word)) continue;
                if (word.equals("")) continue;
                Integer n = count.get(word);
                count.put(word, (n == null) ? 1 : n + 1);
            }
            PriorityQueue<WordCount> pq = new PriorityQueue<WordCount>();
            for (Entry<String, Integer> entry : count.entrySet()) {
                pq.add(new WordCount(entry.getKey(), entry.getValue()));
            }
            words = new ArrayList<WordCount>();
            while (!pq.isEmpty()) {
                WordCount wc = pq.poll();
                if (wc.word.length() > 1) words.add(wc);
            }
            // 단어카운트 끝

            
            String tags = "";
        	String title_side = "";
        	int tag_count = 0;
            
        	// 카운트 이용 제목 및 태그 만들기
            for (WordCount wc : words) {

        		if (wc.word.contains("http")) continue;
        		if (wc.word.contains(searchItem)) continue;
        		//if(filtering(wc.word)) continue;
        		if(tag_count++ < 30) tags = tags + wc.word + ",";
        		if(tag_count < 10) title_side = title_side + wc.word + ", ";
        		else if(tag_count == 10) title_side = title_side + wc.word;
        		
        		
        	}
            // 카운트 이용 제목 및 태그 만들기 끝
            
            ArrayList<String> PhotoList = new ArrayList<String>();
        	
        	String tistory_content = "";
        	
        	// 사진 추출
        	for (Status tweet : tweets) {
        		
        		String s = new String(tweet.getText());
        		
        		
        		
        		if(filter19(s)) continue;
        		
        		if(tweet.getMediaEntities().length != 0)
        		{

        			for(MediaEntity me : tweet.getMediaEntities())
        			if(me.getType().equals("photo")) {
        				//System.out.println("2");
        				if(!PhotoList.contains(me.getMediaURL())) {
        					PhotoList.add(me.getMediaURL());
        					
        				}
        			}
        			

        		}

        	}
        	// 사진 추출 끝
        	
        	int item_num = 0; 
    		int photo_position = 0;
    		
    		tistory_content = tistory_content + "<div class=\"tt_article_useless_p_margin\"><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
        	
    		for (Status tweet : tweets) {
    			if(tweet.isRetweet() == true) continue;

    			String s = new String(tweet.getText());
    			
    			while(s.contains("https://t.co/")) {

        			int i = s.indexOf("https://t.co/");
        			int last_char = i + 23;
        			if(s.length() <= (i+23)) last_char = s.length()-1;

        			s = s.replace(s.substring(i, last_char), "");

        			
        		}
        		
    			s = remove_comma(s);
    			
    			//s = new String(s.getBytes());
    			
    			tistory_content = tistory_content + "<tr><td width=\"60\" align=\"center\" style=\"border: 1px solid rgb(204, 204, 204); font-size: 10px; padding: 1px;\">";
    			tistory_content = tistory_content + "<a href=\"https://twitter.com/" + (String)tweet.getUser().getScreenName() + "\" target=\"_blank\" title=\"" + tweet.getUser().getScreenName() + "\"> ";
    			tistory_content = tistory_content + "<img src=\"" + tweet.getUser().getProfileImageURL() + "\" border=0 title=\"" + tweet.getUser().getName() + "&#13;@" + tweet.getUser().getScreenName() + "\" alt=\"" + tweet.getUser().getScreenName() + "\" onerror=\"this.src='http://abs.twimg.com/sticky/default_profile_images/default_profile_4_normal.png';\"><br>";
  
    			tistory_content = tistory_content + "</a></td><td style=\"border: 1px solid rgb(204, 204, 204); padding: 5px;\">";
    			tistory_content = tistory_content + "<font title=\"" + tweet.getCreatedAt() + "\" style=\"CURSOR:hand;\">" + s + "</font>";
    			tistory_content = tistory_content + "</td> </tr>";

    			item_num++;
    			if(item_num == 14) tistory_content = tistory_content + "</tbody></table><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody><tr><td style=\"text-align:center; padding:1px;\"><p style=\"text-align:center;\"><style>.twitsideAd { width: 336px; height: 280px; margin-top: 15px; margin-bottom: 15px; }@media(max-width: 768px) { .twitsideAd { width: 300px; height: 250px; margin-top: 15px; margin-bottom: 15px; } }</style><script async=\"\" src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script><ins class=\"adsbygoogle twitsideAd\" style=\"display:inline-block\" data-ad-client=\"ca-pub-8716569569929004\" data-ad-slot=\"6723223174\" data-ad-format=\"auto\"></ins><script>(adsbygoogle = window.adsbygoogle || []).push({});</script></p></td></tr></tbody></table><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%\"><tbody>";
    			else if((item_num % 7) == 0){

    				if(photo_position < PhotoList.size()) {
    					
    							tistory_content = tistory_content + "</tbody></table><p style=\"text-align: center; clear: none; float: none;\"><span class=\"imageblock\" style=\"display:inline-block;;height:auto;max-width:100%\"><span dir=\"";
    							
    							tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" rel=\"lightbox\" target=\"_blank\"><img src=\"";
    							tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" style=\"max-width:100%;height:auto\" alt=\"" + searchItem + ", " + title_side + "\"></span></span></p><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
    				
    							photo_position++;
    							

    				}   				
    				
    			}
	
        	}

    		for(int i = 0; i < 5; i++) {
				if(photo_position < PhotoList.size()) {

					tistory_content = tistory_content + "</tbody></table><p style=\"text-align: center; clear: none; float: none;\"><span class=\"imageblock\" style=\"display:inline-block;;height:auto;max-width:100%\"><span dir=\"";
					
					tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" rel=\"lightbox\" target=\"_blank\"><img src=\"";
					tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" style=\"max-width:100%;height:auto\" alt=\"" + searchItem + ", " + title_side + "\"></span></span></p><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse;\"><tbody>";
					
					photo_position++;
					
				}
			}
    		
    		
    		tistory_content = tistory_content + "</tbody></table><p><br></p>";
    		
    		//tistory_content = "안녕하세요";

			TistoryClient TC = new TistoryClient();
			TistoryBrainDotsArticle tistoryBrainDotsArticle = new TistoryBrainDotsArticle();
		
		    tistoryBrainDotsArticle.setTitle("'" + searchItem + "' " + title_side);
		    tistoryBrainDotsArticle.setContent(tistory_content + " ");
		    tistoryBrainDotsArticle.setTag((String)tags);
		   // tistoryBrainDotsArticle.setStrategy(list[2]);
		    tistoryBrainDotsArticle.setVisibility("3");
		    
		    tistoryBrainDotsArticle.setCategory("638206");
	
		    TC.write(tistoryBrainDotsArticle);
		    
		    //Thread.sleep(10000);
		    
		    Set_TimerOn(1);
		    Set_TimerOn(1);
		    Set_TimerOn(1);

        }
        catch (TwitterException te) {
           te.printStackTrace();
           System.out.println("Failed to search tweets: " + te.getMessage());
           //System.exit(-1);
       }
        
    } 
    public static String remove_comma(String s) {
    	s = s.replaceAll("@", "");
		s = s.replaceAll("#", "");
		s = s.replaceAll("ㅋ", "");
		s = s.replaceAll("ㅎ", "");
		s = s.replaceAll("ㅠ", "");
		s = s.replaceAll("ㅜ", "");
		s = s.replaceAll("ㅡ", "");
		s = s.replaceAll("ㅇ", "");
		//s = s.replaceAll("(", "");
		//s = s.replaceAll(")", "");
		//s = s.replaceAll("[", "");
		//s = s.replaceAll("]", "");
		s = s.replace('(', ' ');
		s = s.replace(')', ' ');
		s = s.replace('[', ' ');
		s = s.replace(']', ' ');
		s = s.replaceAll("RT", "");
    	return s;
    	
    	
    }
    public static String remove_broadcast(String s) {
    	s = s.replaceAll("KBS", "");
		s = s.replaceAll("SBS", "");
		s = s.replaceAll("MBC", "");
		s = s.replaceAll("JTBC", "");
		s = s.replaceAll("tvN", "");
		
    	return s;
    	
    }
    
    public static boolean filter19(String s) {
    	boolean check = false;
    	

    		//String s = new String(tweet.getText());
    		//if(s.contains("1383")) check = true;
    		if(s.contains("싸롱")) check = true;
    		else if(s.contains("풀싸롱")) check = true;
    		else if(s.contains("강남풀싸롱")) check = true;
    		else if(s.contains("오피")) check = true;
    		else if(s.contains("바둑이")) check = true;
    		//if(s.contains("https://t.co/")) check = true;
    		else if(s.contains("강남스마트")) check = true;
    		else if(s.contains("강남두바이")) check = true;
    		else if(s.contains("역삼포커스")) check = true;
    		else if(s.contains("강남더블업")) check = true;
    		else if(s.contains("강남풀미러")) check = true;
    		else if(s.contains("역삼힐링")) check = true;
    		else if(s.contains("강남미러룸")) check = true;
    		else if(s.contains("강남힐링")) check = true;
    		else if(s.contains("도우미주점")) check = true;
    		else if(s.contains("사설토토")) check = true;
    		else if(s.contains("스포츠토토")) check = true;
    		else if(s.contains("사설토토사이트추천")) check = true;
    		else if(s.contains("네임드사다리사이트")) check = true;
    		else if(s.contains("네임드사다리")) check = true;
    		else if(s.contains("사다리사이트")) check = true;
    		else if(s.contains("사다리놀이터추천")) check = true;
    		else if(s.contains("바카라")) check = true;
    		else if(s.contains("풀사롱")) check = true;
    		else if(s.contains("소라넷")) check = true;
    		else if(s.contains("카지노")) check = true;
    		else if(s.contains("야동")) check = true;
    		else if(s.contains("바둑이")) check = true;
    		else if(s.contains("토토")) check = true;
    		else if(s.contains("추천인")) check = true;
    		else if(s.contains("룸살롱")) check = true;
    		else if(s.contains("봇")) check = true;
    		else if(s.contains("bot")) check = true;
    		else if(s.contains("강남야구장")) check = true;
    		else if(s.contains("010")) check = true;
    		else if(s.contains("선릉안마")) check = true;
    		else if(s.contains("삼성안마")) check = true;
    		else if(s.contains("학동안마")) check = true;
    		else if(s.contains("논현안마")) check = true;
    		else if(s.contains("역삼안마")) check = true;
    		else if(s.contains("방이동안마")) check = true;
    		else if(s.contains("동대문안마")) check = true;
    		else if(s.contains("종로안마")) check = true;
    		else if(s.contains("일산안마")) check = true;
    		else if(s.contains("서초안마")) check = true;
    		else if(s.contains("방배동안마")) check = true;
    		else if(s.contains("방배안마")) check = true;
    		else if(s.contains("잠실안마")) check = true;
    		else if(s.contains("청담안마")) check = true;
    		else if(s.contains("방배동안마")) check = true;
    		else if(s.contains("시인나게 놀")) check = true;
    		else if(s.contains("모이새오")) check = true;
    		else if(s.contains("무료머니")) check = true;
    		else if(s.contains("야사")) check = true;
    		else if(s.contains("걸그룹합성")) check = true;
    		else if(s.contains("성인조건만남")) check = true;
    		else if(s.contains("다시보기")) check = true;
    		else if(s.contains("최신주소")) check = true;
    		else if(s.contains("돌부처쨩")) check = true;
    		
    		else if(s.contains("야마토사이트")) check = true;
    		else if(s.contains("플라이게임")) check = true;
    		else if(s.contains("스크린경마장")) check = true;
    		else if(s.contains("인터넷경마")) check = true;
    		else if(s.contains("일레븐게임")) check = true;
    		else if(s.contains("후레쉬맞고")) check = true;
    		else if(s.contains("띵동실시간스코어")) check = true;
    		else if(s.contains("337게임")) check = true;
    		
    		else if(s.contains("호프게임")) check = true;
    		else if(s.contains("337게임")) check = true;
    		
    		else if(s.contains("소액결제")) check = true;
    		else if(s.contains("상품권매입")) check = true;
    		else if(s.contains("상품권현금화")) check = true;
    		else if(s.contains("모바일상품권현금화")) check = true;
    		else if(s.contains("정보이용료현금화")) check = true;
    		else if(s.contains("소액결제현금")) check = true;
    		else if(s.contains("아이폰소액결제")) check = true;
    		else if(s.contains("제노사이드")) check = true;
    		
    		else if(s.contains("아포칼립스")) check = true;
    		else if(s.contains("337게임")) check = true;
    		
    		else if(s.contains("골프채브랜드")) check = true;
    		else if(s.contains("경마분석")) check = true;
    		else if(s.contains("경륜경기")) check = true;
    		else if(s.contains("히트경마")) check = true;
    		else if(s.contains("하나캐피탈")) check = true;
    		else if(s.contains("라이브게임")) check = true;
    		else if(s.contains("스크린경마")) check = true;
    		else if(s.contains("주식로또")) check = true;
    		
    		else if(s.contains("홍콩마카오여행")) check = true;
    		else if(s.contains("안전한놀이터")) check = true;
    		
    		else if(s.contains("일본사이버경마")) check = true;
    		else if(s.contains("식보룰")) check = true;
    		else if(s.contains("사설경마")) check = true;
    		else if(s.contains("홀덤사이트")) check = true;
    		else if(s.contains("포커사이트")) check = true;
    		
    		else if(s.contains("실내경마")) check = true;
    		else if(s.contains("인터넷훌라")) check = true;
    		

    		else if(s.contains("퇴폐스크린골프장")) check = true;
    		else if(s.contains("프로토")) check = true;
    		else if(s.contains("소액결제")) check = true;   
    		else if(s.contains("강원랜드")) check = true;
    		else if(s.contains("포커")) check = true;
    		else if(s.contains("야마토")) check = true;
    		else if(s.contains("홀덤")) check = true;
    		
    		
    		
    		
    		else if(s.contains("포커사이트")) check = true;
    		
    		else if(s.contains("실내경마")) check = true;
    		else if(s.contains("인터넷훌라")) check = true;
    		

    		else if(s.contains("퇴폐스크린골프장")) check = true;
    		else if(s.contains("프로토")) check = true;
    		else if(s.contains("소액결제")) check = true;   
    		//if(s.contains("강남스마트")) check = true;
    		
    		//if(s.contains("MAMAredcarpet")) check = true;
    		//if(s.contains("MAMAredcarpert")) check = true;
    			
    	return check;
    }
    

    private static String removePunctuations(String str) {
        return str.replaceAll("\\p{Punct}|\\p{Digit}", "");
    }
    
    
    public static void Set_TimerOn(int nTimer)  throws Exception  // nTimer - 단위 : 초
    {
         int nDelayTime;
         nDelayTime = nTimer * 1000*60; // 밀리초 단위에 맞도록 *1000을 해준다.
        

         Robot tRobot = new Robot();
        tRobot.delay(nDelayTime);   // delay() 함수를 이용하여 nDelayTime 밀리초 동안 프로세스를 sleep 상태로 만든다.
   }
}
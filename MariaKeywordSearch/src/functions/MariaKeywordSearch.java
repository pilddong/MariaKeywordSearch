package functions;
import java.awt.Robot;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Scanner;

import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;   

public class MariaKeywordSearch { 
    public static void main(String[] args) throws Exception {
    	
    	ConfigurationBuilder cb = new ConfigurationBuilder();
    	cb.setDebugEnabled(true)
    	  .setOAuthConsumerKey("o7oKG8OmnlErJ95d5gsH4PDPZ")
    	  .setOAuthConsumerSecret("L2Q2HukCBoRVWo25x51whjCbCkEgjaSKPz6UzFMStrSFXlZpLn")
    	  .setOAuthAccessToken("597051361-jlLoTnMN6SfWsqIJdglQgWmimk6TWflaTLTwHR8C")
    	  .setOAuthAccessTokenSecret("m2OsUtmnJ7KhihUuxGeRfEZSimmFPCytSQABq1K0SOsbs");
    	TwitterFactory tf = new TwitterFactory(cb.build());
    	Twitter twitter = tf.getInstance();
    	
    	
    	Filtering filtering = new Filtering();
        
        ArrayList<String> searchlist = new ArrayList<String>();
        java.util.Scanner input = new java.util.Scanner(new File("./search.txt"),"UTF-8"); 
       // int j = 0; 
        while( input.hasNextLine() ) { 
        	
           searchlist.add(input.nextLine()); 
         //  searchlist.add("나무");
          
        }
        input.close();
       
        // 검색어 추출 끝
        
        System.out.println(searchlist);
        
        ArrayList<WordCount> words;
        
        // 검색아이템 하나씩 모두 for문
        for(String searchItem : searchlist)
        try {
        	String str = "";
       	 	//Query query = new Query(searchItem + " exclude:hashtags");
        	Query query = new Query(searchItem);
            query.setCount(200);	            
            query.setLang("ko");

            int repeat = 10;

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
   	    		
   	    		//if(tweets.get(i).isRetweet() == true) {
   	    		//	tweets.remove(i--);
   	    		//	continue;
   	    		//}

   	    		/*
   	    		if(tweets.get(i).getURLEntities().length != 0) {
   	    			tweets.remove(i--);
   	    			continue;
        		}
        		*/
        		
        		
        		//System.out.println(s);
            }
            HashMap<String, Integer> TextDupCheckListTitle = new HashMap<>();
            for (Status tweet : tweets) {
           	 
            	if(tweet.isRetweet() == true) continue;

           		if(tweet.getURLEntities().length != 0) {

           			continue;
           		}
           		
           		String s = tweet.getText();
           		
           		while(s.contains("https://t.co/")) {

        			int j = s.indexOf("https://t.co/");
        			int last_char = j + 23;
        			if(s.length() < (j+23)) last_char = s.length();

        			s = s.replace(s.substring(j, last_char), "");

        			
        		}
           		s = removeAlphaNumeric(s);
           		if(TextDupCheckListTitle.get(s) != null) continue;
   	  			TextDupCheckListTitle.put(s, 1);

            	//str+=tweet.getText() + "\n";
            	str+=tweet.getText().replaceAll("[(]|[)]", " ") + "\n";

            }
            
            //Filtering filtering = new Filtering();
            
            Scanner scan = new Scanner(str);
            HashMap<String, Integer> count = new HashMap<String, Integer>();
            // 단어 카운트 하기
            while (scan.hasNext()) {
            	
                String word = removePunctuations(scan.next());
                word = remove_comma(word);
                word = remove_broadcast(word);
                if (filtering.filteringList().contains(word)) continue;
                if (filtering.filteringList().contains(word.toLowerCase())) continue;
                if (filtering.filteringList().contains(word.toUpperCase())) continue;
                if (word.equals("")) continue;
                Integer n = count.get(word);
                count.put(word, (n == null) ? 1 : n + 1);
            }
            scan.close();
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

            
            String tags = searchItem + ",";
        	String title_side = "";
        	int tag_count = 0;
            
        	// 카운트 이용 제목 및 태그 만들기
            for (WordCount wc : words) {
            	boolean dup = false;
        		if (wc.word.contains("http")) continue;
        		if (wc.word.contains(searchItem)) continue;
        		for(String split : searchItem.split(" ")) {
        			if (wc.word.contains(split)) dup = true;
        			else if (wc.word.contains(split.toLowerCase())) dup = true;
        			else if (wc.word.contains(split.toUpperCase())) dup = true;
        		}
        		if (dup) continue;
        		if(searchItem.contains(wc.word)) {
        			continue;
        		}
        		if(tag_count++ < 30) tags = tags + wc.word + ",";
        		if(tag_count < 10) title_side = title_side + wc.word + " ";
        		else if(tag_count == 10) title_side = title_side + wc.word;
        		
        		
        	}
            // 카운트 이용 제목 및 태그 만들기 끝
            
            ArrayList<String> PhotoList = new ArrayList<String>();
        	
        	String tistory_content = "";
        	
        	// 사진 추출
        	for (Status tweet : tweets) {
        		
        		String s = new String(tweet.getText());
        		
        		s = remove_comma(s);
        		
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
        		
        		if(PhotoList.size() > 0) break;

        	}
        	// 사진 추출 끝
        	
        	int item_num = 0; 
    		int photo_position = 0;
    		
    		HashMap<String, Integer> TextDupCheckList = new HashMap<>();	  
    		tistory_content = tistory_content + "<div class=\"tt_article_useless_p_margin\"><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
    		boolean AdCheck = false;
    		for (Status tweet : tweets) {
    			if(tweet.isRetweet() == true) continue;
    			
    			if(tweet.getURLEntities().length != 0) continue;

    			String s = new String(tweet.getText());
    			
    			while(s.contains("https://t.co/")) {

        			int i = s.indexOf("https://t.co/");
        			int last_char = i + 23;
        			if(s.length() < (i+23)) last_char = s.length();

        			s = s.replace(s.substring(i, last_char), "");

        			
        		}
    			
    			while(s.contains("http://t.co/")) {
    				
        			int i = s.indexOf("http://t.co/");
        			int last_char = i + 23;
        			if(s.length() < (i+23)) last_char = s.length();

        			s = s.replace(s.substring(i, last_char), "");

        			
        		}
    			
    			while(s.contains("@")) {
    				
        			int i = s.indexOf("@");
        			int last_char = i + 5;
        			if(s.length() < (i+5)) last_char = s.length();

        			s = s.replace(s.substring(i, last_char), "****");

        			
        		}
        		
    			s = remove_comma(s);
    			
    			//s = new String(s.getBytes());
    			//if(TextDupCheckList.get(s) != null) continue;
    			//TextDupCheckList.put(s, 1);
    			
    			String removeAll = removeAlphaNumeric(s);
    			if(TextDupCheckList.get(removeAll) != null) continue;
    			TextDupCheckList.put(removeAll, 1);
    			
    			tistory_content = tistory_content + "<tr><td width=\"60\" align=\"center\" style=\"border: 1px solid rgb(204, 204, 204); font-size: 10px; padding: 1px;\">";
    			tistory_content = tistory_content + "<a href=\"https://twitter.com/" + (String)tweet.getUser().getScreenName() + "\" target=\"_blank\" title=\"" + tweet.getUser().getScreenName() + "\"> ";
    			tistory_content = tistory_content + "<img src=\"" + tweet.getUser().getProfileImageURL() + "\" border=0 title=\"" + tweet.getUser().getName() + "&#13;@" + tweet.getUser().getScreenName() + "\" alt=\"" + tweet.getUser().getScreenName() + "\" onerror=\"this.src='http://abs.twimg.com/sticky/default_profile_images/default_profile_4_normal.png';\"><br>";
  
    			tistory_content = tistory_content + "</a></td><td style=\"border: 1px solid rgb(204, 204, 204); padding: 5px;\">";
    			tistory_content = tistory_content + "<font title=\"" + tweet.getCreatedAt() + "\" style=\"CURSOR:hand;\">" + s + "</font>";
    			tistory_content = tistory_content + "</td> </tr>";

    			item_num++;
    			if(item_num == 14) {
    				AdCheck = true;
    				tistory_content = tistory_content + "</tbody></table><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody><tr><td style=\"text-align:center; padding:1px;\"><p style=\"text-align:center;\"><style>.twitsideAd { width: 336px; height: 280px; margin-top: 15px; margin-bottom: 15px; }@media(max-width: 768px) { .twitsideAd { width: 300px; height: 250px; margin-top: 15px; margin-bottom: 15px; } }</style><script async=\"\" src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script><ins class=\"adsbygoogle twitsideAd\" style=\"display:inline-block\" data-ad-client=\"ca-pub-8716569569929004\" data-ad-slot=\"6723223174\" data-ad-format=\"auto\"></ins><script>(adsbygoogle = window.adsbygoogle || []).push({});</script></p></td></tr></tbody></table><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
    			}
    			else if((item_num % 7) == 0){

    				if(photo_position < PhotoList.size()) {
    					
    							tistory_content = tistory_content + "</tbody></table><p style=\"text-align: center; clear: none; float: none;\"><span class=\"imageblock\" style=\"display:inline-block;;height:auto;max-width:100%;\"><span dir=\"";
    							
    							tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" rel=\"lightbox\" target=\"_blank\"><img src=\"";
    							tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" style=\"max-width:100%;height:auto\" alt=\"" + searchItem + " " + title_side + " " + tweet.getUser().getScreenName() + "\"></span></span></p><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
    				
    							photo_position++;
    							

    				}   	
    				/*
    				else {
    					tistory_content = tistory_content + "</tbody></table><p style=\"text-align: center; clear: none; float: none;\"><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
    				}
    				*/
    				
    			}
	
        	}
			if(!AdCheck) tistory_content = tistory_content + "</tbody></table><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody><tr><td style=\"text-align:center; padding:1px;\"><p style=\"text-align:center;\"><br><br><br>Ads<br><style>.twitsideAd { width: 336px; height: 280px; margin-top: 15px; margin-bottom: 15px; }@media(max-width: 768px) { .twitsideAd { width: 300px; height: 250px; margin-top: 15px; margin-bottom: 15px; } }</style><script async=\"\" src=\"//pagead2.googlesyndication.com/pagead/js/adsbygoogle.js\"></script><ins class=\"adsbygoogle twitsideAd\" style=\"display:inline-block\" data-ad-client=\"ca-pub-8716569569929004\" data-ad-slot=\"6723223174\" data-ad-format=\"auto\"></ins><script>(adsbygoogle = window.adsbygoogle || []).push({});</script></p></td></tr></tbody></table><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
			if(photo_position < PhotoList.size()) tistory_content = tistory_content + "</tbody></table><p><br><br><br></p><p style=\"text-align: center; clear: none; float: none;\">" + searchItem + "</p><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse; width:100%;\"><tbody>";
    		for(int i = 0; i < 5; i++) {
				if(photo_position < PhotoList.size()) {

					tistory_content = tistory_content + "</tbody></table><p style=\"text-align: center; clear: none; float: none;\"><span class=\"imageblock\" style=\"display:inline-block;;height:auto;max-width:100%;\"><span dir=\"";
					
					tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" rel=\"lightbox\" target=\"_blank\"><img src=\"";
					tistory_content = tistory_content + PhotoList.get(photo_position).toString() + "\" style=\"max-width:100%;height:auto\" alt=\"" + searchItem + " " + title_side + "\"></span></span></p><table align=\"center\" style=\"color: rgb(0, 0, 0); font-family: 'Malgun Gothic'; border-collapse: collapse;\"><tbody>";
					
					photo_position++;
					
				}
			}
    		
    		
    		tistory_content = tistory_content + "</tbody></table><p><br></p></div>";
    		
    		//tistory_content = "안녕하세요";

			TistoryClient TC = new TistoryClient();
			TistoryBrainDotsArticle tistoryBrainDotsArticle = new TistoryBrainDotsArticle();
		
		    //tistoryBrainDotsArticle.setTitle("'" + searchItem + "' " + title_side);
		    tistoryBrainDotsArticle.setTitle(searchItem + " " + title_side);
		    tistoryBrainDotsArticle.setContent(tistory_content + " ");
		    tistoryBrainDotsArticle.setTag((String)tags);
		   // tistoryBrainDotsArticle.setStrategy(list[2]);
		    tistoryBrainDotsArticle.setVisibility("3");
		    
		    tistoryBrainDotsArticle.setCategory("638206");
	
		    TC.write(tistoryBrainDotsArticle);
		    
		    //Thread.sleep(10000);
		    
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
		    Set_TimerOn(60);
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
		else if(s.contains("JAM-44")) check = true;   
		else if(s.contains("찬성의원")) check = true; 
		else if(s.contains("비례대표")) check = true;   
		else if(s.contains("경마")) check = true;
		
		else if(s.contains("4U")) check = true;
		else if(s.contains("Sex")) check = true;
    	
		else if(s.contains("씨발")) check = true;
		else if(s.contains("시발")) check = true;
		else if(s.contains("시바")) check = true;
		else if(s.contains("씨바")) check = true;
		else if(s.contains("병신")) check = true;
		else if(s.contains("KST")) check = true;
		else if(s.contains("포커")) check = true;
		
		
		else { // 조건 
			String filter = "스폰 야짤 애인모드 섹ㅅ 더보기 무비프리 급구 대리티켓팅 쓰리썸 쓰리섬 음성추출 mp3 대포폰 알뜰폰 선불유심 신불노출매입 선불폰 후불폰 꽁머니 SUK33 재생목록 장터TV 접속주소 to-jt 섹파 엑스골프 고고게임 골프스카이 엑사이엔씨 마진콜 중국명주수정방 베이시스위험 경정예상지 금요경륜 온라인도박 스크린골프 추천주소 밤문화 강남물고기 홀짝 성욕 강원랜드 19다모아 dominostory 발기 고자 양도 폰팅 폰섹 여고생폰팅 보이스채팅 야한대화 섹스만남 미스폰 애인만들기 전화만남 세ㄱ파 url href www object embed script swf meta onload onclick onmouseover sex porn poker viagra cialis lama 바카라 카지노 바다이야기 야마토 고스톱 포커 섹스 포르노 대출 신용불량 조건만남 애인대행 대포통장 handa10 viagra cialis drug woori10 업소여성 s다이어리 zot10 연예지망생 모델지망생 sedek S.E.X tara2010 rana58 Partner airen69 nana58 best58 bojo69 OneLove kading 좆 토렌트 ㅈㄱ CS olleh BSTODAY 상위단어 창녀 프리미어리그픽 세리에 xaza 네임드 허니픽 usd79 magnet 대포통장 와꾸 마사지 콘돔 오르가즘 베팅 배팅 poltra 안마 원나잇 시오후키 섹그램 안전놀이터 립카페 유흥 분당안마 서현안마 정자안마 야탑안마 미금안마 수원안마 인천안마 간석안마 송내안마 부평안마 부천안마 일산안마 라페스타안마 백석동안마 화정안마 고양시안마 마두역안마 정발산안마 안마걸 안마녀 안마후기 펄안마 자전거안마 잭팟안마 백송안마 설야안마 가인안마 스타안마 안마시술 레인보우안마 휴게텔 페티쉬 건마 키스방 O1O OIO 비아그라 클럽옥타곤 테이블강남 초원의집 북창동식 강남하드룸 강남접대룸 강남밤문화 강남접대장소 강남하드코어 강남식스 강남샌즈 강남더킹 강남벅시 강남마카오 옥타곤 강남노래방 야노 야외노출 베팅 블랙잭 경마 포커 홀덤 미친년 미친놈 ㅅㅂ 시벌 스벌 18아 18놈 18새끼 18년 18뇬 18노 18것 18넘 개년 개놈 개뇬 개새 개색끼 개세끼 개세이 개쉐이 개쉑 개쉽 쓰벌 쓰팔 씨8 씨댕 씨바 씨발 씨뱅 씨봉알 씨부랄 씨부럴 씨부렁 씨부리 씨불 씨브랄 씨빠 씨빨 씨뽀랄 씨팍 씨팔 씨펄 씹 아가리 아갈이 엄창 접년 잡놈 재랄 저주글 조까 조빠 조쟁이 조지냐 조진다 조질래 존나 존니 좀물 좁년 좃 좆 좇 쥐랄 쥐롤 쥬디 지랄 지럴 지롤 지미랄 쫍빱 凸 퍽큐 뻑큐 빠큐 ㅅㅂㄹㅁ 개시키 개자식 개좆 게색기 게색끼 광뇬 뇬 눈깔 뉘미럴 니귀미 니기미 니미 도촬 되질래 뒈져라 뒈진다 디져라 디진다 디질래 병쉰 병신 뻐큐 뻑큐 뽁큐 삐리넷 새꺄 쉬발 쉬밸 쉬팔 쉽알 스패킹 스팽 시벌 시부랄 시부럴 시부리 시불 시브랄 시팍 시팔 시펄 실밸 십8 십쌔 십창 싶알 쌉년 썅놈 쌔끼 쌩쑈 썅 써벌 썩을년 쎄꺄 쎄엑 쓰바 쓰발 시발 씨발 시바";
			for(String filterSplit : filter.split(" ")) {
				if(s.equals("")) continue;
				if(s.contains(filterSplit)) {
					check = true;
					break;
				}
			}
		}
		//if(s.contains("강남스마트")) check = true;
		
		//if(s.contains("MAMAredcarpet")) check = true;
		//if(s.contains("MAMAredcarpert")) check = true;
    			
    	return check;
    }
    

    private static String removePunctuations(String str) {
       // return str.replaceAll("\\p{Punct}", "");
        //return str.replaceAll("\\p{Punct}|\\p{ASCII}", "");
    	
    	String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        str = str.replaceAll(match, "");
        
        String s = str.replaceAll("\\p{Punct}|\\p{ASCII}", "");
        if(!s.equals("")) return str.replaceAll("\\p{Punct}", "");
        else return s;
    }
    /*
    private static String removePunctuationAll(String str) {
        //return str.replaceAll("\\p{Punct}|\\p{Digit}", "");
    	String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        str = str.replaceAll(match, "");
        //return str.replaceAll("\\p{Punct}", ""); //\p{ASCII}
        return str.replaceAll("\\p{Punct}|\\p{ASCII}", "");
        
    }
    */
    
    private static String removeAlphaNumeric(String str) {
        //return str.replaceAll("\\p{Punct}|\\p{Digit}", "");
    	//String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
        //str = str.replaceAll(match, "");
        //return str.replaceAll("\\p{Punct}", ""); //\p{ASCII}
        return str.replaceAll("\\p{Alnum}|\\p{Blank}", "");
        
    }
    
    
    public static void Set_TimerOn(int nTimer)  throws Exception  // nTimer - 단위 : 초
    {
         int nDelayTime;
         nDelayTime = nTimer * 1000; // 밀리초 단위에 맞도록 *1000을 해준다.
        

         Robot tRobot = new Robot();
        tRobot.delay(nDelayTime);   // delay() 함수를 이용하여 nDelayTime 밀리초 동안 프로세스를 sleep 상태로 만든다.
   }
}
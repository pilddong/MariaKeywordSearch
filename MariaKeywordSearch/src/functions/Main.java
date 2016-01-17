package functions;

import java.awt.Robot;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			MariaYahooJPDailyWord.main(args);
			MariaYahooJPDailyPeople.main(args);
			MariaYahooJPDailyFood.main(args);
			while(true) {
				MariaYahooJPRealtimeWord.main(args);
				MariaYahooJPRealtimeTwit.main(args);
				MariaYahooJPRealtimeImage.main(args);
				MariaYahooJPRealtimeVideo.main(args);
				MariaYahooJPRealtimePeople.main(args);
				for(int i = 0; i < 150; i++)
				Set_TimerOn(1);
				Set_TimerOn(1);
				Set_TimerOn(1);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void Set_TimerOn(int nTimer)  throws Exception  // nTimer - 단위 : 분
    {
         int nDelayTime;
         nDelayTime = nTimer * 1000 * 60; // 밀리초 단위에 맞도록 *1000을 해준다.
        

         Robot tRobot = new Robot();
        tRobot.delay(nDelayTime);   // delay() 함수를 이용하여 nDelayTime 밀리초 동안 프로세스를 sleep 상태로 만든다.
   }

}

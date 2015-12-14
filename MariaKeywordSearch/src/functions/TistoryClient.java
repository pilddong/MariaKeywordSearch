package functions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
/**
 * @see "http://www.tistory.com/guide/api/post"
 * API �낅슣�섓옙�봳tp://twitiwt.tistory.com/api ID1955909�좎럥梨룟폑�낆삕占쎌눘異놂옙�쒖구�됵옙臾억㏄�턈HYXG7
 */
public class TistoryClient {
    private static final String ACCESS_TOKEN = "3930d8223c4014fd65afa06919fe0f0f_9792a5bd6ecbce32f7698fbf7fa0d34b";

    private static final String CLIENT_ID = "a363cf6eb6f8ac2c1cd7cfde928aae74";
    private static final String SECRET_KEY = "a363cf6eb6f8ac2c1cd7cfde928aae7487ec663f7f1fe88bc5d8d74df6f13fa125f01e7d";
    private static final String REDIRECT_URI = "http://168.131.153.174:8080/TwitterMiningEngine/result.jsp";

    private static final String TARGET_URL = "mariashelt";
    private static final String WRITE_API_URL = "https://www.tistory.com/apis/post/write";
    private static final String MODIFY_API_URL = "https://www.tistory.com/apis/post/modify";
    private static final String CATEGORY_LIST_API_URL = "https://www.tistory.com/apis/category/list";

    // 占쎈슔�뗩�占썹뛾�됱삕�됯릿�좑옙�ㅲ뵛
    private static final String DONATE = "<iframe src=\"http://gift.blog.daum.net/widget?entryId=0&amp;setNo=2043\" width=\"100%\" height=\"250\" frameborder=\"0\" border=\"0\" scrolling=\"no\" allowtransparency=\"true\" ;=\"\"></iframe>";

    private static final String OUTPUT = "json";

    private static final String GAME_CATEGORY = "607233";

    private static final String USER_AGENT = "Mozilla/5.0";

    public void getAccessToken() {
        String clientId = "a363cf6eb6f8ac2c1cd7cfde928aae74";
        String clientSecret = "a363cf6eb6f8ac2c1cd7cfde928aae7487ec663f7f1fe88bc5d8d74df6f13fa125f01e7d";
        String redirectUri = "http://168.131.153.174:8080/TwitterMiningEngine/result.jsp";
        String grantType = "authorization_code";

//        String requestUrl = "https://www.tistory.com/oauth/access_token/?code=" + authorization_code +
//                "&client_id=" + CLIENT_ID +
//                "&client_secret=" + SECRET_KEY +
//                "&redirect_uri=" + REDIRECT_URI +
//                "&grant_type=" + grantType;
    }
    /**
     * write tistory article
     * @param tistoryBrainDotsArticle
     */
    public void write(TistoryBrainDotsArticle tistoryBrainDotsArticle) throws IOException {
        // common validation check
        checkCommonValidation(tistoryBrainDotsArticle);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(WRITE_API_URL);

        // add header
        post.setHeader("User-Agent", USER_AGENT);

        // String title = "占쎈맧��옙�됱삕�좎룞�숃쥈�뗫콦 �좎뜫�뗰옙誘�삕占쎄내��" + tistoryBrainDotsArticle.getStage() + " �좎럥占쏙옙怨살삕�좎룞�숋옙�됯껀�좎룞��
        String title = tistoryBrainDotsArticle.getTitle();


        String content = tistoryBrainDotsArticle.getContent();
       
        String category = tistoryBrainDotsArticle.getCategory();
       
        //        "<h2>占쎈맧��옙�됱삕�좎룞�숃쥈�뗫콦 �좎뜫�뗰옙誘�삕占쎄내��" + tistoryBrainDotsArticle.getTitle() + " �좎럥占쏙옙怨살삕�좑옙h2>" +
        //                "<div>" + tistoryBrainDotsArticle.getYoutube() + "</div>\n" +
        //                "<div>" + tistoryBrainDotsArticle.getStrategy() + "</div>\n" +
        //                "<div>" + DONATE + "</div>\n";

        //Set<String> tagSets = getBrainDotsTags();

        //tagSets.add(tistoryBrainDotsArticle.getTitle());

        //String tags = Joiner.on(",").join(tagSets);
        String tags = tistoryBrainDotsArticle.getTag();
        System.out.println(tags);

        List urlParameters = Lists.newArrayList();
        urlParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        urlParameters.add(new BasicNameValuePair("targetUrl", TARGET_URL)); // �곗뒪�좊━ 二쇱냼. http://xxx.tistory.com �쇨꼍��xxx 留��낅젰, 2李⑤룄硫붿씤��寃쎌슦 http://�쒓굅��url �낅젰
        urlParameters.add(new BasicNameValuePair("output", OUTPUT));    // output type

        urlParameters.add(new BasicNameValuePair("title", title));  // �쒕ぉ
        urlParameters.add(new BasicNameValuePair("content", content));  // �댁슜
        urlParameters.add(new BasicNameValuePair("category", category));   // 移댄뀒怨좊━
        urlParameters.add(new BasicNameValuePair("tag", tags)); // �쒓렇
        
        if (tistoryBrainDotsArticle.getVisibility() != null) {
            urlParameters.add(new BasicNameValuePair("visibility", tistoryBrainDotsArticle.getVisibility()));
            System.out.println(tistoryBrainDotsArticle.getVisibility());
        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));

        HttpResponse response = client.execute(post);

        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
    }

    /*
    public void modify(TistoryBrainDotsArticle tistoryBrainDotsArticle) throws Exception {
        // validation check for modify
        if (tistoryBrainDotsArticle.getPostId() == null) {
            throw new RuntimeException("postId needed");
        }

        // common validation check
        checkCommonValidation(tistoryBrainDotsArticle);


        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(MODIFY_API_URL);

        // add header
        post.setHeader("User-Agent", USER_AGENT);

        String title = "占쎈맧��옙�됱삕�좎룞�숃쥈�뗫콦 �좎뜫�뗰옙誘�삕占쎄내��" + tistoryBrainDotsArticle.getStage() + " �좎럥占쏙옙怨살삕�좎룞�숋옙�됯껀�좎룞��

       String content =
                "<h2>占쎈맧��옙�됱삕�좎룞�숃쥈�뗫콦 �좎뜫�뗰옙誘�삕占쎄내��" + tistoryBrainDotsArticle.getStage() + " �좎럥占쏙옙怨살삕�좑옙h2>" +
                "<div>" + tistoryBrainDotsArticle.getYoutube() + "</div>" +
                "<div>" + tistoryBrainDotsArticle.getStrategy() + "</div>" +
                "<div>" + DONATE + "</div>\n";

        Set<String> tagSets = getBrainDotsTags();

        tagSets.add(tistoryBrainDotsArticle.getStage());

        String tags = Joiner.on(",").join(tagSets);

        List urlParameters = Lists.newArrayList();
        urlParameters.add(new BasicNameValuePair("access_token", ACCESS_TOKEN));
        urlParameters.add(new BasicNameValuePair("targetUrl", TARGET_URL)); //
        urlParameters.add(new BasicNameValuePair("title", title));  // �좎럩裕놅옙占�       urlParameters.add(new BasicNameValuePair("content", content));  // �좎럥�삼옙占�       urlParameters.add(new BasicNameValuePair("category", GAME_CATEGORY));   // �곸궠�억옙誘ㅿ옙�ル벣遊�        urlParameters.add(new BasicNameValuePair("tag", tags)); // �좎럩裕꾬옙占�       urlParameters.add(new BasicNameValuePair("output", OUTPUT));    // output type

        urlParameters.add(new BasicNameValuePair("postId", tistoryBrainDotsArticle.getPostId()));   // only modify

        post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));

        HttpResponse response = client.execute(post);

        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
    }
    */

    protected void checkCommonValidation(TistoryBrainDotsArticle tistoryBrainDotsArticle) {
       /* if (tistoryBrainDotsArticle.getYoutube() == null) {
            throw new RuntimeException("youtube needed");
        }

        if (tistoryBrainDotsArticle.getStage() == null) {
            throw new RuntimeException("stage needed");
        }

        if (tistoryBrainDotsArticle.getStrategy() == null) {
            throw new RuntimeException("strategy needed");
        }
        */
        
        if (tistoryBrainDotsArticle.getTitle() == null) {
            throw new RuntimeException("title needed");
        }
        if (tistoryBrainDotsArticle.getContent() == null) {
            throw new RuntimeException("content needed");
        }
    }


    /**
     * brain dots tags
     * @return tags set
     */
    private Set<String> getBrainDotsTags() {
        Set<String> tagSets = Sets.newHashSet();
        tagSets.add("占쎈맧��옙�됱삕筌뤾퍓利꿨뜝�숈삕");
        tagSets.add("braindots");
        tagSets.add("brain dots");
        tagSets.add("�롪퍓�ｏ옙占�");
        tagSets.add("game");
        tagSets.add("�좎럩�섇럴占�");
        tagSets.add("puzzle");
        tagSets.add("占썩뫀踰됵옙占�");
        tagSets.add("strategy");
        tagSets.add("�좎럥占쏙옙怨살삕�좑옙");
        tagSets.add("clear");
        tagSets.add("占쎈�梨띰옙占�");
        tagSets.add("brain");

        return tagSets;
    }

    /**
     * get category list
     * @throws IOException
     */
    public void categoryList() throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        String output = "json";
        HttpGet request = new HttpGet(CATEGORY_LIST_API_URL + "?access_token=" + ACCESS_TOKEN + "&targetUrl=" + TARGET_URL + "&output=" + output);

        // add header
        request.setHeader("User-Agent", USER_AGENT);

        HttpResponse response = client.execute(request);

        System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
    }
}
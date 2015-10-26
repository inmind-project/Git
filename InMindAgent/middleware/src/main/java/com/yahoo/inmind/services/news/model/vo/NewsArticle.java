package com.yahoo.inmind.services.news.model.vo;


import android.text.Html;

import com.google.gson.internal.LinkedTreeMap;
import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.control.Util;
import com.yahoo.inmind.services.news.control.util.JsonUtil;

import org.json.simple.JSONObject;

import java.util.ArrayList;


public class NewsArticle extends JsonItem {
	public final static short DIM_UNDEFINED = -1;
	public final static short DIM_PORTRAIT = 0;
	public final static short DIM_LANDSCAPE = 1;

	private Integer idx;
    private String title;
    private String uuid;
    private String summary;
    private String imgUrl;
    private String publisher;
    private String score;
    private String url;
    private String reason;
    private String imgPath;					//For ImgLruCache
    private Short dimension = DIM_UNDEFINED; //For ImgLruCache
    private double dwellTime = 0.0;
    private Boolean like;
    private Boolean dislike;
    private boolean clickOnNews = false;
    private boolean isSharedInFb = false;
    private boolean isSharedInTwitter = false;
    private boolean isSharedInTumblr = false;
    private boolean isShareInMore = false;
    private ArrayList<Category> categories = new ArrayList<>();
    private ArrayList<CapFeature> capFeatures = new ArrayList<>();
    private ArrayList<RawScore> rawScores= new ArrayList<>();
    private String userComments;

    //attributes for control
    private transient boolean isVisited = false;
    private transient double rank;
    private transient Object tvComments;
    private transient boolean recommendation1 = false; // Emma
    private transient boolean recommendation2 = false; // William


    public NewsArticle() {
		super();
	}

    public NewsArticle(int idx, JSONObject jobj){
        super();
        this.idx = new Integer(idx);
        this.title = Html.fromHtml( JsonUtil.getProp(jobj, Constants.ARTICLE_TITLE) ).toString();
        this.uuid = JsonUtil.getProp(jobj, Constants.ARTICLE_UUID);
        this.publisher = Html.fromHtml( JsonUtil.getProp(jobj, Constants.ARTICLE_PUBLISHER) ).toString();
        this.summary = Html.fromHtml( JsonUtil.getProp(jobj, Constants.ARTICLE_SUMMARY) ).toString();
        this.imgUrl = JsonUtil.getProp(jobj, Constants.ARTICLE_IMAGE_URL);
        this.score = JsonUtil.getProp(jobj, Constants.ARTICLE_SCORE);
        this.url = JsonUtil.getProp(jobj, Constants.ARTICLE_URL);
        this.reason = Html.fromHtml( JsonUtil.getProp(jobj, Constants.ARTICLE_REASON) ).toString();
        Object catTemp = Util.fromJsonList(JsonUtil.getProp(jobj, Constants.ARTICLE_CATEGORIES), categories.getClass());
        if (catTemp != null && ((ArrayList<LinkedTreeMap>) catTemp).isEmpty() == false) {
            for (LinkedTreeMap ltm : (ArrayList<LinkedTreeMap>) catTemp) {
                Category cat = new Category();
                cat.setId((String) ltm.get("id"));
                cat.setMembershipScore((Double) ltm.get("membershipScore"));
                cat.setDisplayName( Html.fromHtml( (String) ltm.get("displayName") ).toString() );
                categories.add(cat);
            }
        }
        String[] capFeatTemp = Util.replaceAll( JsonUtil.getProp(jobj, Constants.ARTICLE_CAP_FEATURES), "[{}]", "").split( "," );
        if (capFeatTemp != null && capFeatTemp.length > 0 ) {
            for (String feat : capFeatTemp) {
                CapFeature feature = new CapFeature();
                String[] elements = feat.split( "\":" );
                feature.setName( Html.fromHtml( elements[0] ).toString() );
                if( elements.length < 2 ) {
                    feature.setWeight(new Double(0.0));
                }else {
                    feature.setWeight(new Double(elements[1]));
                }
                capFeatures.add( feature );
            }
        }

        String[] rawScoreTemp = Util.replaceAll( JsonUtil.getProp(jobj, Constants.ARTICLE_RAW_SCORE_MAP), "[{}]", "").split( "," );
        if (rawScoreTemp != null && rawScoreTemp.length > 0 ) {
            for (String scoreString : rawScoreTemp) {
                //Log.e("DEBUG","scoreString: "+scoreString);
                RawScore rawScore = new RawScore();
                String[] elements = scoreString.split( "\":" );
                rawScore.setName( Html.fromHtml( elements[0] ).toString() );
                if( elements.length < 2 ){
                    rawScore.setWeight( new Double(0.0) );
                }else {
                    rawScore.setWeight(new Double(elements[1]));
                }
                rawScores.add( rawScore );
            }
        }

    }

    public Integer getIdx() {
        return idx;
    }


    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = Html.fromHtml( title ).toString();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = Html.fromHtml( summary ).toString();
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = Html.fromHtml( publisher ).toString();
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = Html.fromHtml( reason ).toString();
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Short getDimension() {
        return dimension;
    }

    public void setDimension(Short dimension) {
        this.dimension = DIM_LANDSCAPE; // it must be always portrait otherwiswe it won't shown
    }

    public void setDwellTime(double dwellTime) {
        this.dwellTime = dwellTime;
    }

    public double getDwellTime(){
        return dwellTime;
    }

    public void setDislike(Boolean dislike) {
        this.dislike = dislike;
        this.like = !dislike;
    }

    public void setLike(Boolean like) {
        this.like = like;
        this.dislike = !like;
    }

    public Boolean isLike() {
        return like;
    }

    public void setClickOnNews(boolean clickOnNews) {
        this.clickOnNews = clickOnNews;
    }

    public Object getTvComments() {
        return tvComments;
    }

    public void setTvComments(Object tvComments) {
        this.tvComments = tvComments;
    }

    public void setVisited(boolean isVisited) {
        this.isVisited = isVisited;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getUserComments() {
        return userComments;
    }

    public void setUserComments(String userComments) {
        this.userComments = Html.fromHtml( userComments ).toString();
    }

    public ArrayList<CapFeature> getCapFeatures() {
        return capFeatures;
    }

    public void setCapFeatures(ArrayList<CapFeature> capFeatures) {
        this.capFeatures = capFeatures;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public ArrayList<RawScore> getRawScores() {
        return rawScores;
    }

    public void setRawScores(ArrayList<RawScore> rawScores) {
        this.rawScores = rawScores;
    }


    public class CapFeature{
        private String name;
        private Double weight;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        @Override
        public String toString(){
            return "Name: " + name + "    Weight:" + weight;
        }
    }

    public class RawScore{
        private String name;
        private Double weight;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }

        @Override
        public String toString(){
            return "Name: " + name + "    Weight:" + weight;
        }
    }

    public class Category{
        private String id;
        private double membershipScore;
        private String displayName;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public double getMembershipScore() {
            return membershipScore;
        }

        public void setMembershipScore(double membershipScore) {
            this.membershipScore = membershipScore;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }


    public boolean isRecommendation1() {
        return recommendation1;
    }

    public void setRecommendation1(boolean recommendation1) {
        this.recommendation1 = recommendation1;
    }

    public boolean isRecommendation2() {
        return recommendation2;
    }

    public void setRecommendation2(boolean recommendation2) {
        this.recommendation2 = recommendation2;
    }

    @Override
    public String toString(){
        return title + "  dwell time: " + dwellTime;
    }

}

/*

    AVAILABLE FIELDS IN THE JsonUtil
	•	[0] = {java.util.HashMap$HashMapEntry@830056054952}"appropriateness" -> "0"
	•	[1] = {java.util.HashMap$HashMapEntry@830056346944}"sddocname" -> "ca"
	•	[2] = {java.util.HashMap$HashMapEntry@830055309744}"summaryfeatures" -> newsSize = 9
	•	[3] = {java.util.HashMap$HashMapEntry@830056238768}"score" -> "1.1024"
	•	[4] = {java.util.HashMap$HashMapEntry@830055997904}"embargo" -> "1419429604"
	•	[5] = {java.util.HashMap$HashMapEntry@830056212248}"snippet" -> newsSize = 25
	•	[6] = {java.util.HashMap$HashMapEntry@830055920544}"publisher" -> "Yahoo Style"
	•	[7] = {java.util.HashMap$HashMapEntry@830056251512}"id" -> "bd9467d7-ec6c-39c8-824c-2e18cca4d5d2"
	•	[8] = {java.util.HashMap$HashMapEntry@830056009616}"raw_score" -> "0.24840002042054046"
	•	[9] = {java.util.HashMap$HashMapEntry@830055951672}"title" -> "If Your Guy Does These 5 Things, DON'T Marry Him!"
	•	[10] = {java.util.HashMap$HashMapEntry@830055949624}"has_best_tweet" -> "0"
	•	[11] = {java.util.HashMap$HashMapEntry@830055913064}"raw_score_map" -> newsSize = 2
	•	[12] = {java.util.HashMap$HashMapEntry@830056080336}"features" -> newsSize = 7
	•	[13] = {java.util.HashMap$HashMapEntry@830055930472}"url_domain" -> "yahoo.com"
	•	[14] = {java.util.HashMap$HashMapEntry@830056272960}"prediction_score" -> "1.1024"
	•	[15] = {java.util.HashMap$HashMapEntry@830056234600}"cluster_topic" -> "bd9467d7-ec6c-39c8-824c-2e18cca4d5d2"
	•	[16] = {java.util.HashMap$HashMapEntry@830056236816}"content_type" -> "ca_st"
	•	[17] = {java.util.HashMap$HashMapEntry@830055986352}"title_tokens" -> newsSize = 5
	•	[18] = {java.util.HashMap$HashMapEntry@830055952784}"cap_features" -> newsSize = 32
	•	[19] = {java.util.HashMap$HashMapEntry@830055954520}"hr_tier10" -> "false"
	•	[20] = {java.util.HashMap$HashMapEntry@830055956880}"match_type" -> "Popularity"
	•	[21] = {java.util.HashMap$HashMapEntry@830055963216}"hr_valid" -> "true"
	•	[22] = {java.util.HashMap$HashMapEntry@830056054544}"story_cluster" -> "TitleToken:8a98f524-e02c-37b1-a1b6-c816293bc7a8,Entity:bd9467d7-ec6c-39c8-824c-2e18cca4d5d2"
	•	[23] = {java.util.HashMap$HashMapEntry@830055952104}"publish_time" -> "1419429604"
	•	[24] = {java.util.HashMap$HashMapEntry@830055930928}"sourceid" -> "yahoo.com/style"
	•	[25] = {java.util.HashMap$HashMapEntry@830055956136}"uuid" -> "bd9467d7-ec6c-39c8-824c-2e18cca4d5d2"
	•	[26] = {java.util.HashMap$HashMapEntry@830056320936}"explain" -> newsSize = 2
	•	[27] = {java.util.HashMap$HashMapEntry@830055918080}"hosted" -> "true"
 */
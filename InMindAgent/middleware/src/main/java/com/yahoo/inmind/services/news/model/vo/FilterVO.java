package com.yahoo.inmind.services.news.model.vo;

import com.yahoo.inmind.commons.control.Constants;
import com.yahoo.inmind.commons.rules.model.PropositionalStatement;

import java.util.ArrayList;

/**
 * Created by oscarr on 1/9/15.
 */
public class FilterVO extends PropositionalStatement {

    public FilterVO(String attribute, String operator, String value) {
        super(attribute, operator, value);
        componentName = Constants.NEWS;
    }

    /**
     * Returns true if the article doesn't fit into the filter criteria (it must be removed),
     * false otherwise
     * @param obj is the news article
     * @return
     */
    @Override
    public Object validate( Object obj ) {
        NewsArticle article = (NewsArticle) obj;
        if ( attribute.equals(Constants.ARTICLE_SCORE) ||
                attribute.equals(Constants.ARTICLE_RAW_SCORE_MAP)) {
            return !validateNumbers( (Double) convertAttribute( this.attribute, article),
                    Double.valueOf( this.value ));
        } else if ( attribute.equals(Constants.ARTICLE_INDEX)) {
            return !validateNumbers( (Integer) convertAttribute( this.attribute, article),
                    Integer.valueOf( this.value ));
        } else if (attribute.equals(Constants.ARTICLE_CATEGORIES)) {
            ArrayList<NewsArticle.Category> categories =
                    (ArrayList<NewsArticle.Category>) convertAttribute(attribute, article);
            String value = this.value;
            boolean hasValue = false;
            for(NewsArticle.Category category : categories ){
                if( operator.equals(Constants.OPERATOR_EQUALS_TO)
                        ? category.getId().substring(4).equals(value )  //we use 4 because yct: is the prefix
                        : category.getId().substring(4).contains(value)){
                    hasValue = true;
                    break;
                }
            }
            if( hasValue == false ){
                return true;
            }
        } else {
            return !validateStrings( (String) convertAttribute(this.attribute, article) );
        }
        return false;
    }

    public Object convertAttribute( String attribute, NewsArticle item ){
        if( attribute.equals( Constants.ARTICLE_SCORE ) ){
            return item.getScore() != null? Double.valueOf( item.getScore() ) : new Double(-1);
        }
        if( attribute.equals( Constants.ARTICLE_RAW_SCORE_MAP ) ){
            return item.getRawScores();
        }
        if( attribute.equals( Constants.ARTICLE_CAP_FEATURES ) ){
            return item.getCapFeatures();
        }
        if( attribute.equals( Constants.ARTICLE_IMAGE_URL ) ){
            return item.getImgUrl()  != null? item.getImgUrl() : "";
        }
        if( attribute.equals( Constants.ARTICLE_INDEX ) ){
            return item.getIdx() != null? Integer.valueOf(item.getIdx()) : new Integer(-1);
        }
        if( attribute.equals( Constants.ARTICLE_PUBLISHER ) ){
            return item.getPublisher() != null? item.getPublisher() : "";
        }
        if( attribute.equals( Constants.ARTICLE_REASON ) ){
            return item.getReason() != null? item.getReason() : "";
        }
        if( attribute.equals( Constants.ARTICLE_SUMMARY ) ){
            return item.getSummary() != null? item.getSummary() : "";
        }
        if( attribute.equals( Constants.ARTICLE_TITLE ) ){
            return item.getTitle() != null? item.getTitle() : "";
        }
        if( attribute.equals( Constants.ARTICLE_URL ) ){
            return item.getUrl() != null? item.getUrl() : "";
        }
        if( attribute.equals( Constants.ARTICLE_UUID ) ){
            return item.getUuid() != null? item.getUuid() : "";
        }
        if( attribute.equals( Constants.ARTICLE_CATEGORIES ) ){
            return item.getCategories();
        }
        return null;
    }
}

package com.oplay.giftassistant.model.data.resp;

import java.io.Serializable;
import java.util.List;

/**
 * @author micle
 * @email zsigui@foxmail.com
 * @date 2015/12/20
 */
public class SearchPromptResult implements Serializable {

    public String keyword;
    public List<String> promtList;
}

package skhu.ht.hotthink.api.idea.model;

import lombok.Getter;
import lombok.Setter;
import skhu.ht.hotthink.api.domain.Category;
import skhu.ht.hotthink.api.domain.User;

import java.util.Date;

public class FreeOutDTO {
    @Getter @Setter private Category category;
    @Getter @Setter private Long seq;
    @Getter @Setter private int hits;
    @Getter @Setter private String title;
    @Getter @Setter private Date date;
    @Getter @Setter private String contents;
    @Getter @Setter private int good;
    @Getter @Setter private String image;
    @Getter @Setter private User user;
}
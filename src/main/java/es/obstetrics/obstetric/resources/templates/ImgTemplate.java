package es.obstetrics.obstetric.resources.templates;

import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.StreamResource;

public class ImgTemplate extends HorizontalLayout {

    public ImgTemplate(String src, String alt, String height){
        Image logoImg = new Image(src, alt);
        logoImg.setHeight(height);
        add(logoImg);
    }
    public ImgTemplate(StreamResource src,String height){
        Image image = new Image();
        image.setHeight(height);
        image.setSrc(src);
        add(image);
    }

}

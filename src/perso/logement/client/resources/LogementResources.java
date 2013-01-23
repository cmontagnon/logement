package perso.logement.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface LogementResources extends ClientBundle {
  @Source("down.png")
  ImageResource down();

  @Source("up.png")
  ImageResource up();

  @Source("green.png")
  ImageResource green();

  @Source("red.png")
  ImageResource red();
}

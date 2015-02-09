package jp.cspiral.mosaica;

/**
 * @author tomita
 */

import java.io.IOException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.mongodb.MongoException;

@Path("/")
public class MosaicA {

	private final ImageController controller = new ImageController();

	@POST
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/pushImage")
	public Response pushImage(@FormParam("img") final String img, @FormParam("key") final String key,
			@FormParam("divx") final int divx, @FormParam("divy") final int divy) throws InterruptedException {
		String imageId;
//		System.out.println("img: " + img);
		try {
			imageId = controller.createParentImage(img,key,divx,divy);
			return Response.status(200).entity(imageId).build();
		} catch (IOException e) {
			return Response.status(200).entity("failue").build();
		}
	}

	@GET
	@Produces({MediaType.APPLICATION_XML})
	@Path("/getImage")
	public Response getImage(@QueryParam("imageId") final String imageId) {
		return Response.status(200).entity(controller.getImage(imageId)).build();
	}

	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/getImageIdList")
	public Response getImageIdList() {
		return Response.status(200).entity(controller.getImageIdList()).build();
	}

	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Path("/saveImage")
	public Response saveImage(@QueryParam("imageId") final String imageId) {
		try {
			return Response.status(200).entity(controller.saveImage(imageId)).build();
		} catch (MongoException e) {
			return Response.status(200).entity("MongoException").build();
		} catch (IOException e) {
			return Response.status(200).entity("IOException").build();
		}
	}
}

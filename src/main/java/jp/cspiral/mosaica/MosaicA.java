package jp.cspiral.mosaica;

/**
 * @author tomita
 */

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import twitter4j.auth.AccessToken;
import jp.cspiral.mosaica.sns.TwitterAuthController;

import com.mongodb.MongoException;

@Path("/")
public class MosaicA {

	private final ImageController controller = new ImageController();
	@SuppressWarnings("unused")
	private HttpSession session;

	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/pushImage")
	public Response pushImage(@FormParam("img") final String img,
			@FormParam("key") final String key,
			@FormParam("divx") final int divx, @FormParam("divy") final int divy)
			throws InterruptedException {
		String imageId;
		// System.out.println("img: " + img);
		try {
			imageId = controller.createParentImage(img, key, divx, divy);
			return Response.status(200).entity(imageId).build();
		} catch (IOException e) {
			return Response.status(200).entity("failue").build();
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_XML })
	@Path("/getImage")
	public Response getImage(@QueryParam("imageId") final String imageId) {
		return Response.status(200).entity(controller.getImage(imageId))
				.build();
	}

	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/getImageIdList")
	public Response getImageIdList() {
		return Response.status(200).entity(controller.getImageIdList()).build();
	}

	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/saveImage")
	public Response saveImage(@QueryParam("imageId") final String imageId) {
		try {
			return Response.status(200).entity(controller.saveImage(imageId))
					.build();
		} catch (MongoException e) {
			return Response.status(200).entity("MongoException").build();
		}
	}

	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path("/twitterRequest")
	public Response twitterRequest(@Context HttpServletRequest request,
			@QueryParam("imageId") String imageId) {
		TwitterAuthController tc = new TwitterAuthController();
		String authURL = tc.requestToken(request.getSession(), imageId);
		URI uri;
		try {
			uri = new URI(authURL);
			return Response.seeOther(uri).build();
//			return Response.ok().entity(authURL).build();
		} catch (URISyntaxException e) {
			return Response.status(200).entity(e.toString()).build();
		}
	}

	@GET
	@Path("/twitterUpdate")
	public Response twitterUpdate(@Context HttpServletRequest request,
			@QueryParam("oauth_token") String oAuthToken,
			@QueryParam("oauth_verifier") String oAuthVerifier) {
		TwitterAuthController tc = new TwitterAuthController();
		HttpSession session = request.getSession();
		AccessToken accessToken = tc.getAccessToken(oAuthToken, oAuthVerifier,
				session);
		try {
			String status = tc.uploadImage(accessToken,
					(String) session.getAttribute("imageId"));
			return Response.ok().entity(status).build();
		} catch (NullPointerException e) {
			return Response.ok().entity("エラーが発生しました。もう一度試してみてください。").build();
		}
	}
}

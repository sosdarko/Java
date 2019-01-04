package main;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.*;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@Path("")
public class WDBCService {

	static WDBCDispatcher dispatcher = new WDBCDispatcher();

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public void OnStartUp() {
		;
	}

	@GET
	@Path("/health")
	@Produces(javax.ws.rs.core.MediaType.TEXT_PLAIN)
	public String getHealth() {
		return "I'm OK";
	}

	@GET
	@Path("/")
	@Produces(javax.ws.rs.core.MediaType.TEXT_HTML)
	public String welcome() {
		String s;
		try {
			s = readFile("Welcome.html", StandardCharsets.UTF_8);
		} catch (IOException e) {
			s = "Welcome!";
		}
		return s;
	}

	@POST
	@Path("/DB")
	@Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	@Produces(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	public String DBCall(String message) {
		// TODO: napravi genericke WDBCResponse i WDBCDispatcher
		// probaj da izvedes WDBCQueryResponse iz WDBCResponse i vidi kako radi Gson!
		// Logovanje u katalini!
		System.out.println(message);
		WDBCCommand comm;
		String jResp = null;
		WDBCResponse sResp;
		Gson gsonI = new Gson();
		Gson gsonO = new Gson();

		// try to convert the message into WDBCCommand instance
		try {
			comm = gsonI.fromJson(message, WDBCCommand.class);
		} catch (JsonSyntaxException e) {
			System.out.println(e.getMessage());
			comm = null;
		}
		if (comm != null) {
			sResp = dispatcher.Dispatch(comm);
			jResp = gsonO.toJson(sResp);
			// TODO: sve poruke o gresci treba da idu u neki enum, zajedno kod i tekst
			// nesto kao: ERR_UNSUPPORTED_VERB -> (100,"Unsupported verb")
		} else {
			sResp = new WDBCResponse(100, "Invalid request format");
			jResp = gsonO.toJson(sResp);
		}

		return jResp;
	}
}

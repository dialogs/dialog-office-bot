package com.dialogs.JSDBot;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

import com.dialogs.JSDBot.config.DialogConfig;
import com.dialogs.JSDBot.config.JSDConfig;
import com.dialogs.JSDBot.utils.Request;
import com.dialogs.JSDBot.utils.RequestField;
import com.dialogs.JSDBot.utils.RequestFieldValue;
import com.dialogs.JSDBot.utils.RequestType;

import im.dlg.botsdk.Bot;
import im.dlg.botsdk.BotConfig;
import im.dlg.botsdk.domain.InteractiveEvent;
import im.dlg.botsdk.domain.Message;
import im.dlg.botsdk.domain.Peer;
import im.dlg.botsdk.domain.content.Content;
import im.dlg.botsdk.domain.content.DocumentContent;
import im.dlg.botsdk.domain.interactive.InteractiveAction;
import im.dlg.botsdk.domain.interactive.InteractiveButton;
import im.dlg.botsdk.domain.interactive.InteractiveGroup;
import im.dlg.botsdk.domain.interactive.InteractiveSelect;
import im.dlg.botsdk.domain.interactive.InteractiveSelectOption;
import im.dlg.botsdk.domain.media.FileLocation;
import im.dlg.botsdk.light.InteractiveEventListener;
import im.dlg.botsdk.light.MessageListener;

public class JSDBot implements MessageListener, InteractiveEventListener {

	private BotConfig botConfig;
	private Bot bot;
	private RequestHandler requestHandler;
	
	public JSDBot( DialogConfig dialogConfig, JSDConfig jsdConfig ) throws Exception{
		this.botConfig = BotConfig.Builder
			.aBotConfig()
			.withHost(dialogConfig.getHost())
			.withPort(dialogConfig.getPort())
			.withToken(dialogConfig.getToken())
			.build();
		
		this.requestHandler = new RequestHandler(jsdConfig);
	}
	
	@Override
	public void onMessage(Message message) {
		
		Content content = message.getMessageContent();
		Peer sender = message.getSender();
		String contentType = content.getClass().getSimpleName();
		boolean hasCurrentRequest = this.requestHandler.hasCurrentRequest(sender.getId());
		
		 bot.users().get(message.getSender()).thenAccept(userOpt -> userOpt.ifPresent(user -> {
             System.out.println("Got a message: " + message.getText() + " from user: " + user);
             System.out.println(user.getCustomProfile());
             System.out.println(user.getAbout());
         }));
 
		try {
			if ( contentType.equals("DocumentContent") ) {
				
				DocumentContent documentContent = (DocumentContent) content;
				FileLocation fileLocation = new FileLocation(documentContent.getFileId(), documentContent.getAccessHash());

				if ( !hasCurrentRequest || !this.requestHandler.getCurrentFieldType(sender.getId()).equals("attachment") ) throw new UserInputException("Sorry, I don't know what to do with this file");
				
				String fileName = documentContent.getName();
				
				String fileUrl = bot.mediaAndFilesApi().getFileUrl(FileLocation.buildFileLocation(fileLocation)).get();	
				String hashnamesStringArrayByComas = this.requestHandler.uploadAttachmentFile(fileName, fileUrl);
				
				this.setRequestField(sender, hashnamesStringArrayByComas, true);
				
			} else if ( contentType.equals("TextContent") ) {	
				
				String messageText = message.getText();
				
				if ( hasCurrentRequest ) {
					this.setRequestField(sender, messageText, false);
				} else {
					this.findRequestTypes(sender, messageText);
				}
			} else {
				throw new UserInputException("Sorry, I don't know what to do with this");
			}
		} catch ( UserInputException e ) {
			this.bot.messaging().sendText(sender, e.getMessage());
		} catch ( Exception e ) {
			this.bot.messaging().sendText(sender, "Sorry, something goes wrong ...");
			e.printStackTrace();
		}
	}

	@Override
	public void onEvent(InteractiveEvent event) {
		System.out.println(event.toString());
		
		String eventId = event.getId();
		String eventValue = event.getValue();
		Peer eventPeer = event.getPeer();
		
		try {
			if ( eventId.equals("create") ) {
				this.createRequest(eventPeer, eventValue);
			} else if ( eventId.equals("cancel") ) {
				this.cancelRequest(eventPeer);
			} else if ( eventId.equals("send") ) {
				this.sendRequest(eventPeer);
			} else if ( eventId.equals("set") ) {
				this.setRequestField(eventPeer, eventValue, false);
			} else {
				throw new UserInputException("Sorry, i don't understand what you want");
			}
		} catch ( UserInputException e ) {
			this.bot.messaging().sendText(eventPeer, e.getMessage());
		} catch ( Exception e ) {
			this.bot.messaging().sendText(eventPeer, "Sorry, something goes wrong ...");
			e.printStackTrace();
		}
	}
	
	public void start() throws Exception {
		this.bot = Bot.start(this.botConfig).get();
		this.bot.messaging().onMessage(this);
		this.bot.interactiveApi().onEvent(this);
		this.bot.await();
	}
	
	private void findRequestTypes( Peer peer, String pattern ) throws Exception {
		List<RequestType> matchingRequestTypes = this.requestHandler.findRequestTypes(pattern);
		
		if ( matchingRequestTypes.size() == 0 ) throw new UserInputException("Sorry, I don't found matching results");
			
		bot.messaging().sendText(peer, "Ok, let me figure it out... Do you need to").get();
		
		for ( int i=0; i<matchingRequestTypes.size(); i++ ) {
			RequestType requestType = matchingRequestTypes.get(i);
			InteractiveGroup group = this.buildInteractiveGroupButton("create", Integer.toString(requestType.getId()), requestType.getName());
			bot.interactiveApi().send(peer, group).get();
		}
	}
	
	private void createRequest( Peer peer, String stringRequestTypeId ) throws UserInputException, Exception {
		try {
			int requestTypeId = Integer.parseInt(stringRequestTypeId);
			
			Request request = this.requestHandler.createRequest(peer.getId(), requestTypeId);
			bot.messaging().sendText(peer, request.getDescription()).get();
			InteractiveGroup cancelButton = this.buildInteractiveGroupButton("cancel", Integer.toString(peer.getId()), "Cancel request");
			bot.interactiveApi().send(peer, cancelButton).get();
			//this.requestHandler.cancelRequest(peer.getId());
			RequestField currentField = this.requestHandler.getCurrentField(peer.getId());
			bot.messaging().sendText(peer, currentField.getName()).get();
		} catch ( NumberFormatException e ) {
			throw new UserInputException("Sorry, but your requestTypeId is invalid");
		}
	}
	
	public void setRequestField( Peer peer, String fieldValue, boolean forceValidation ) throws UserInputException, Exception {
		this.requestHandler.setRequestField(peer.getId(), fieldValue, forceValidation);
		
		if ( this.requestHandler.isRequestCompleted(peer.getId()) ) {
			// request completed
			bot.messaging().sendText(peer, "Ok, got it").get();
			InteractiveGroup cancelButton = this.buildInteractiveGroupButton("cancel", Integer.toString(peer.getId()), "Cancel request");
			bot.interactiveApi().send(peer, cancelButton).get();
			InteractiveGroup sendButton = this.buildInteractiveGroupButton("send", Integer.toString(peer.getId()), "Send request");
			bot.interactiveApi().send(peer, sendButton).get();
		} else {
			// need new field
			RequestField requestField = this.requestHandler.getCurrentField(peer.getId());
			bot.messaging().sendText(peer, requestField.getName()).get();
			// show select options for "validValues"
			if ( requestField.getValidValues().length > 0 ) {
				InteractiveGroup selectOptions = this.buildInteractiveGroupSelect("set", requestField.getValidValues());
				bot.interactiveApi().send(peer, selectOptions).get();	
			}
		}
	}
	
	private void sendRequest( Peer peer ) throws UserInputException, Exception {
		String dialogsUsername = bot.users().get(peer).get().get().getNick();
				
		System.out.println(dialogsUsername);
		
		JSONObject sendRequestJsonResponse = this.requestHandler.sendRequest(peer.getId(), dialogsUsername);
		
		String createdRequestUrl = sendRequestJsonResponse.getJSONObject("_links").getString("web");
		String message = "Great, your request is created: " + createdRequestUrl + ". Now you can ask for the new request.";
		
		bot.messaging().sendText(peer, message).get();
	}
	
	private void cancelRequest( Peer peer ) throws UserInputException, Exception {
		this.requestHandler.cancelRequest(peer.getId());
		
		bot.messaging().sendText(peer, "Your request was cancelled, now you can ask for new requests").get();
	}
		
	private InteractiveGroup buildInteractiveGroupButton( String id, String value, String label ) {
		List<InteractiveAction> actions = new ArrayList<>();
		actions.add(new InteractiveAction(id, new InteractiveButton(value, label)));
		return new InteractiveGroup(actions);
	}
	
	private InteractiveGroup buildInteractiveGroupSelect( String id, RequestFieldValue[] optionPairs ) {
		List<InteractiveSelectOption> selectOptions = new ArrayList<>();
		for ( int i=0; i<optionPairs.length; i++ ) {
			selectOptions.add(new InteractiveSelectOption(optionPairs[i].getValue(), optionPairs[i].getLabel()));
		}
		ArrayList<InteractiveAction> actions = new ArrayList<>();
		actions.add(new InteractiveAction(id, new InteractiveSelect("Chose one ..." , "Chose one ...", selectOptions)));
		return new InteractiveGroup(actions);
	}

}

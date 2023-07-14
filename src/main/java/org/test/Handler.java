package org.test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNS;
import com.amazonaws.services.lambda.runtime.events.SNSEvent.SNSRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.test.model.Header;
import org.test.model.Mail;
import org.test.model.Message;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

public class Handler implements RequestHandler<SNSEvent, List<String>> {

  @SneakyThrows
  @Override
  public List<String> handleRequest(SNSEvent event, Context context) {
    LambdaLogger logger = context.getLogger();

    String dbUrl = "jdbc:mysql://host:3306/test";
    String username = "db_user";
    String password = "password";

    var messagesFound = new ArrayList<String>();

    try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
      String query = "insert into ses_info "
          + "(bounced_recipient, source, header_1, header_2)"
          + "VALUES (?, ?, ?, ?)";

      // Prepare the statement with the INSERT query
      try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {

        for (SNSRecord snsRecord : event.getRecords()) {
          SNS message = snsRecord.getSNS();
          messagesFound.add(message.getMessage());

          ObjectMapper mapper = new ObjectMapper();
          Message messageValues = mapper.readValue(message.getMessage(), Message.class);

          if (!"Bounce".equals(messageValues.getEventType())) {
            continue;
          }
          Mail mail = messageValues.getMail();

          Map<String, String> headers = mail.getHeaders().stream()
              .collect(Collectors.toMap(Header::getName, Header::getValue));

          String bouncedRecipient = mail.getDestination().get(0);
          String source = mail.getSource();
          // Populate customised specific headers
          String header1 = headers.get("Header-1");
          String header2 = headers.get("Header-2");

          preparedStatement.setString(1, bouncedRecipient);
          preparedStatement.setString(2, source);
          preparedStatement.setString(3, header1);
          preparedStatement.setString(4, header2);

          // Execute the INSERT statement
          int rowsAffected = preparedStatement.executeUpdate();
          logger.log("Rows affected: " + rowsAffected);
        }
      }
    } catch (SQLException e) {
      logger.log("SQLException: " + e);
    }

    return messagesFound;
  }
}
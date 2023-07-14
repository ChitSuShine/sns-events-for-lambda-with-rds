# Purpose
- This code repo is to collect Hard Bounce Rate of sending emails with AWS SES
# Terminology
- SES will have Configuration sets with SNS topic as an event destination which has Lambda function as a subscription
- Lambda function will store event type data into RDS
- Customised POJO classes (for Header, Mail & Message) are created as I cannot find out in AWS SDK
# Tech Stacks
- SES, SNS, Lambda, RDS
# Deployment Steps
1. Generate jar with `mvn clean package`
2. Upload as a .zip or .jar file in Lambda Console
3. Test with [Sample sns-notification.json](https://github.com/awsdocs/aws-lambda-developer-guide/blob/main/sample-apps/java-events/events/sns-notification.json)
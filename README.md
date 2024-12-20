# AWS Lambda backend for the Quote app

This AWS Lambda backend 
- fetches quotes from https://zenquotes.io/ and stores them in a DynamoDB table
- is created to learn about SAM CLI and creating a serverless API with Java, AWS Lambda and ApiGateway 
- is exposed through ApiGateway and used by the Quote Web App that can be visited at:
>https://master.d3bgu77svyk8ir.amplifyapp.com/

The code for the Quote Web App can be found at:
>https://github.com/edwinbulter/quote-amplify-react


## Project setup with SAM CLI
[What is SAM CLI?](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam-overview.html#what-is-sam-cli)

### Create an example java lambda:
You can create an example with:

>sam init --runtime java11 --dependency-manager maven --name quote-lambda

But the structure is not ideal. Restructure it by moving src to the root of your project.

### Create a template.yml
Example contents:
```yaml
AWSTemplateFormatVersion: '2010-09-09'
Transform: AWS::Serverless-2016-10-31
Description: An AWS Serverless Application.

Resources:
  QuoteLambda:
    Type: AWS::Serverless::Function
    Properties:
      CodeUri: ./  # local path to Lambda function code
      Handler: ebulter.quote.lambda.QuoteHandler::handleRequest
      Runtime: java17
      Timeout: 30
      Policies:
        - Version: '2012-10-17'
          Statement:
            - Effect: Allow
              Action:
                - dynamodb:Scan
                - dynamodb:PutItem
                - dynamodb:GetItem
              Resource:
                Fn::Sub: arn:aws:dynamodb:${AWS::Region}:${AWS::AccountId}:table/Quotes
```

### Create samconfig.toml
- You can copy this from an example created with sam init
- This is where you set your stack-name

Example contents:
```toml
version = 0.1
[default.deploy.parameters]
stack_name = "quote-lambda"
resolve_s3 = true
s3_prefix = "quote-lambda"
region = "eu-central-1"
capabilities = "CAPABILITY_IAM"
image_repositories = []
```


## Test local (without container):
- sam build
- sam local invoke -e events/event.json

## Test local (with container):
- Create template.yaml
- sam build --use-container
- sam local invoke QuoteLambda -e events/event.json

## Deploy
- https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/using-sam-cli-deploy.html

- The folder .aws-sam contains what will be deployed

- First time:
  - sam build
  - sam deploy --guided
- Next time:
  - sam build
  - sam deploy
- Add Policies for dynamodb to template.yaml:
```yaml
      # Add this to the properties section
      Policies:
        - Version: '2012-10-17'
          Statement:
            - Effect: Allow
              Action:
                - dynamodb:Scan
                - dynamodb:PutItem
                - dynamodb:GetItem
              Resource:
                Fn::Sub: arn:aws:dynamodb:${AWS::Region}:${AWS::AccountId}:table/Quotes
```
- Create the ApiGateway endpoints to template.yaml:
```yaml
      # Add this to the properties section
      Events: # The Events property enables AWS SAM to create an API Gateway
        GetQuote:
          Type: Api
          Properties:
            Path: /quote
            Method: get
        PostQuote:
          Type: Api
          Properties:
            Path: /quote
            Method: post
        PatchLike:
          Type: Api
          Properties:
            Path: /quote/{id}/like
            Method: patch
        GetLiked:
          Type: Api
          Properties:
            Path: /quote/liked
            Method: get
```
- Set CrossOrigin in template.yaml for access from the browser
```yaml
# Add this at the root level just above resources
Globals:
  Api:
    Cors:
      AllowOrigin: "'*'"
      AllowHeaders: "'*'"
      AllowMethods: "'GET, POST, PATCH, OPTIONS'"
```
## Delete the stack:

>aws cloudformation delete-stack --stack-name quote-lambda




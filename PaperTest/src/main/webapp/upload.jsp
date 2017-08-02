<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>upload</title>
</head>
<body>
	<form action="/api/upload/uploadFile.do"  method="post" enctype="multipart/form-data" >
		<input type="file" name="file" id="file">
		<input type="hidden"  value="test@whaty.com">
		<input type="submit" name="submit" value="提交">
		
	</form>
</body>
</html>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8" />
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

<!--[if lt IE 9]>
<script src="/statics/js/html5shiv.js"></script>
<![endif]-->
<link type="text/css" rel="stylesheet" href="http://yunpan.sdk.webtrn.cn/statics/css/upload.css" />
<link type="text/css" rel="stylesheet" href="http://yunpan.sdk.webtrn.cn/statics/css/jquery.ui.plupload.min.css" />
<link type="text/css" rel="stylesheet" href="http://yunpan.sdk.webtrn.cn/statics/css/jquery.plupload.queue.min.css" />
<link type="text/css" rel="stylesheet" href="http://yunpan.sdk.webtrn.cn/statics/css/jquery-ui-1.9.2.min.css" />

<script type="text/javascript">
	var ctxPath = "http://yunpan.sdk.webtrn.cn";
	
	var token="";
	/**
	 * 注册云盘
	 */
	function registerYunPan() {
		var url = "${base}/u/userUploadResource/registerYunPan.json?data=info";
		var data = {};
		var login_id="admin";
		var siteCode="taiji";
		data["page.searchItem.loginId"] = login_id;
		data["page.searchItem.sitecode"] = siteCode;
		$.ajax({
			type : "post",
			dataType : "json",
			url : url,
			data : data,
			error : function(XMLHttpRequest, textStatus, errorThrown) {
			},
			success : function(data) {
				if (data.page.items && data.page.items.length > 0) {
					token = data.page.items[0].token;
				}
			}
		});
	}
	
	registerYunPan();
</script>

</head>

<body class="examples examples-core" style="margin-top: 5px;">

<script type="text/javascript" src="http://yunpan.sdk.webtrn.cn/statics/js/jquery-1.8.3.min.js" charset="UTF-8"></script>
<script type="text/javascript" src="http://yunpan.sdk.webtrn.cn/statics/js/jquery-ui-1.9.2.custom.min.js" charset="UTF-8"></script>
<script type="text/javascript" src="http://yunpan.sdk.webtrn.cn/statics/js/jquery.cookie.js"></script>
<script type="text/javascript" src="http://yunpan.sdk.webtrn.cn/statics/js/plupload.full.min.js" charset="UTF-8"></script>
<script type="text/javascript" src="http://yunpan.sdk.webtrn.cn/statics/js/moxie.min.js?v=0408"></script>
<script type="text/javascript" src="http://yunpan.sdk.webtrn.cn/statics/cloudupload/js/md5.js"></script>
<script type="text/javascript" src="http://yunpan.sdk.webtrn.cn/statics/js/plupload.dev.min.js?v=0408"></script>

<div class="main">
	<div class="container">
		<div class="clearfix"></div>
		<div id="example" class="example" style="height: 370px;">
			<div id="container">
				<div id="pickfiles" class="btn_addPic "></div>
				
			</div>
			<!-- [Select files]  [Upload files]-->
			<div style="clear: both;font-size: 0 !important; height: 0 !important;line-height: 0 !important;" >[Upload files]</div>
			<div id="filelist" class="upload-list-box"><span style="display:none;">Your browser doesn't have Flash, Silverlight or HTML5 support.</span></div>
			<p id="console" class="console" style="display: none;"></p>
			<div style="border: 1px solid #ddd; width: 518px; height: 25px; margin-top: 5px; padding-top: 5px; line-height: 45px; color: #666; background: #f8ebbf; font: 12px/1.5 tahoma, arial, 宋体 !important;">
				&nbsp;&nbsp;&nbsp;&nbsp;新！使用360极速、谷歌、火狐浏览器，可支持秒速上传啦，还能大文件上传哦~
			</div>
			<br />
			<script type="text/javascript">
				var currentPath ="";
				var uploadUrl = ctxPath + "/api/chunkupload";
				// var presecuploadURL = ctxPath + "/api/presecupload";
				var has_upload_success = false;
				
				var createUploader = function(directory) {
					// Custom example logic
					var uploader = new plupload.Uploader({
						//runtimes : 'html5,flash,silverlight,html4',
						runtimes : 'flash',
						browse_button : 'pickfiles', // you can pass in id...
						container : document.getElementById('container'), // ... or DOM Element itself
						url : uploadUrl,
						//presecuploadURL: presecuploadURL,
		                chunk_size : '512kb',
		                multipart: true,
		            	multi_selection: true,
		            	chunks : true,
		            	directory: false || directory,
		            	multipart_params : {
		            		token:"",
		            		path:"",
		            		fileId:""
		            	},
		            	filters : {
		            		exclude_special_chars: null,
		            		max_file_size : '3000mb',
		            		max_file_count: 1
		            		/* mime_types: [
		            			{title : "All files", extensions : "jpg,gif,png,bmp,doc,docx,ppt,pptx,pdf,flv,mp4,zip,rar"},
		            			{title : "Image files", extensions : "jpg,gif,png,bmp"},
		            			{title : "doc files", extensions : "doc,docx,ppt,pptx,pdf"},
		            			{title : "video files", extensions : "flv,mp4"},
		            			{title : "Zip files", extensions : "zip,rar"}
		            		] */
		            	},
						// Flash settings
						flash_swf_url :'/statics/js/Moxie.swf',
						// Silverlight settings
						silverlight_xap_url :'/statics/js/Moxie.xap',

						initUploadMode: function () {
							if (parent.setUploadWinTitle) {
								parent.setUploadWinTitle();
							}
						},
						init : {
							PostInit : function() {
								document.getElementById('filelist').innerHTML = '';
							},
							BeforeUpload : function(uploader,file) {
								currentPath = parent.currentPath || "";
								
								uploader.settings.multipart_params.token = token;
								uploader.settings.multipart_params.path = currentPath;
								uploader.settings.multipart_params.fileId = file.id;
							},
							FilesAdded : function(up, files) {
								up.disableBrowse(true);
								
								for (var i = files.length - 1; i >= 0; i--) {
									var file = files[i];
									$('#filelist').prepend('<div id="' + file.id + '" class="name">' + file.name + ' (' + plupload.formatSize(file.size) + ')<i id="tip" style="font-weight:normal;font-style: normal;"> 等待上传...</i><br> <div style="width: 100%"></div><b></b> </div>');
										
									var result = includeChar(file.path);
									if (result === false) {
									} else {
										document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = "<span>" + result + "</span>";
									}
								}
								
								document.getElementById('pickfiles').className = "uploadfiles ";
								document.getElementById('pickfiles').onclick = function() {
									if (~document.getElementById('pickfiles').className.indexOf("uploadfiles")) {
										uploader.start();
										return false;
									}
								};
							},

							UploadProgress : function(up, file) {
								
								if (!document.getElementById(file.id)) {
									return;
								}
								
								var uploadBytesPerSec = up.total.bytesPerSec;
								var uploadSpeed = "";
								if(uploadBytesPerSec > 1024*1024) {
									uploadSpeed = " " + ((uploadBytesPerSec/(1024*1024)).toFixed(2)) + " mb/s";
								} else if(uploadBytesPerSec > 1024) {
									uploadSpeed = " " + ((uploadBytesPerSec/1024).toFixed(2)) + " kb/s";
								} else {
									uploadSpeed = " " + uploadBytesPerSec + " byte/s";
								}
								document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = '<span>' + file.percent + '%</span><span style="margin-left: 100px;"> ' + uploadSpeed + ' </span>';
								document.getElementById(file.id).getElementsByTagName('i')[0].style.display='none';
								document.getElementById(file.id).getElementsByTagName('div')[0].innerHTML='<div aria-valuenow="0" aria-valuemax="100" aria-valuemin="0" role="progressbar" class="pbar ui-progressbar ui-widget ui-widget-content ui-corner-all"><div style="display: block; width:'+file.percent+'% ;" class="ui-progressbar-value ui-widget-header ui-width-bar ui-corner-left upload-0"></div></div>';

								if(file.percent == '100'){
									var msg = "上传成功";
									if (file.secupload) {
										msg = file.progress;
										
										if (false) {
											has_upload_success = true;
										} else {
											window.parent.DetailViewNeedReflesh = true;
										}
										
										if (has_upload_success && uploader.files[uploader.files.length -1].id == file.id) {
											window.parent.DetailViewNeedReflesh = true;
											has_upload_success = false;
										}
									}
									
									document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = "<span>" + msg + "</span>";
									document.getElementById('pickfiles').className = "btn_addPic ";
								} else if (file.progress && !file.secuploadchunk){
									document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = "<span>" + file.progress + "</span>";
								}
							},

							Error : function(up, err) {
								
								up.disableBrowse(false);
								/* if(err.code == -601) {
									alert("文件格式不正确,只允许上传以下格式的文件：\n jpg,gif,png,bmp,doc,docx,ppt,pptx,pdf,flv,mp4,zip,rar");
								} */
								if(err.response) {
									var jsonReturn = $.parseJSON(err.response);
									if(jsonReturn) {
										var fileId = jsonReturn.fileId;
										document.getElementById(fileId).getElementsByTagName('b')[0].innerHTML = "<span style='color: red;'>上传失败：" + jsonReturn.errorMsg + "</span>";
										document.getElementById('pickfiles').className = "btn_addPic ";
									}									
								} else if (err.code == -605 || err.code == -600) {
									$('#filelist').prepend('<div id="' + err.file.id + '" class="name">不合法文件：' + err.file.name + '<br> <div style="width: 100%"></div><b></b> </div>');
									document.getElementById(err.file.id).getElementsByTagName('b')[0].innerHTML = "<span style='color: red;'>" + err.message + "</span>";
								} else if (!err.file) {
									$('#filelist').prepend('<div class="name">' + err.message + '<br> <div style="width: 100%"></div><b></b> </div>');
								} else {
									document.getElementById(err.file.id).getElementsByTagName('b')[0].innerHTML = "<span style='color: red;'>" + err.message + "</span>";
								}
								
								if (has_upload_success && uploader.files[uploader.files.length -1].id == err.file.id) {
									window.parent.DetailViewNeedReflesh = true;
									has_upload_success = false;
								}
							},
							FileUploaded : function (uploader,file,responseObject) {
								if(responseObject) {
									if(responseObject.response) {
										var jsonReturn = responseObject.response;
										if (typeof jsonReturn === "string") {
											jsonReturn = eval("(" + responseObject.response + ")");
										}
										
										if(jsonReturn) {
											if(jsonReturn.errorCode) {
												var fileId = jsonReturn.fileId;
												document.getElementById(fileId).getElementsByTagName('b')[0].innerHTML = "<span style='color: red;'>上传失败：" + jsonReturn.errorMsg + "</span>";
												document.getElementById('pickfiles').className = "btn_addPic ";
											} else {
												if (jsonReturn.presecupload) {
													document.getElementById(file.id).getElementsByTagName('b')[0].innerHTML = "<span>上传成功，快传……</span>";
												}
												if (false) {
													has_upload_success = true;
												} else {
													window.parent.DetailViewNeedReflesh=true;
												}
											}
										}
									}
								}
								
								if (has_upload_success && uploader.files[uploader.files.length -1].id == file.id) {
									window.parent.DetailViewNeedReflesh = true;
									has_upload_success = false;
								}
								uploader.disableBrowse(false);
							},
							UploadComplete : function(uploader,files) {
								uploader.disableBrowse(false);
							}
						}
					});

					uploader.init();
				}; 
				
				if (false) {
					createUploader("pickfiles", true);
				} else {
					createUploader();
				}
				
				function includeChar(path) {
					var paths = path.split("/");
					var chars = "+,?,%,/,=,#,&,\\\\,:,*,<,>,|".split(",");
					for (var i in paths) {
						var name = paths[i];
						if (name.length > 50) {
							return "文件（夹）名超过50个字符";
						}
						for (var i in chars) {
							if (~name.indexOf(chars[i])) {
								return "文件（夹）名包含特殊字符（+,?,%,/,=,#,&,\\\\,:,*,<,>,|）";
							}
						}
					}
					return false;
				}
			</script>
		</div>
	</div>
</div>
</html>

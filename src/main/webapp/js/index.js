var endpoint = 'http://52.69.136.65:8080/MosaicA/api';

// init
var imgId;
var dataUrl;
console.log("js読み込んだよ");

var selectImage = $("#selectImage")[0];
var fromFileBtn = $("#fromFile")[0];
var fromCameraBtn = $("#fromCamera")[0];

// 「ファイルから」の部品作成
var imageSelectBtn = document.createElement("input");
imageSelectBtn.type = "file";
imageSelectBtn.accept = "image/jpeg";
imageSelectBtn.addEventListener("change", function(evt){
	var file = evt.target.files;
	var fReader = new FileReader();
	fReader.readAsDataURL(file[0]);

	// 読み込み終了時
	fReader.onload = function(){
		console.log(file[0].name);
		if(file[0].name.match( ".jpeg" ) || file[0].name.match( ".jpg" )){
			console.log("ok");
			dataUrl = fReader.result;
			selectImage.innerHTML = "<img src='" + dataUrl + "' width='100%' height='100%'>";
		}
		else{
			alert("please select jpeg file");
			dataUrl = null;
		}
	};
},false);

// 「カメラ」の部品作成
var imageVideo = document.createElement("video");
imageVideo.width = 640;
imageVideo.height = 480;
imageVideo.autoplay = 1;
var imageCanvas = document.createElement("canvas");
imageCanvas.width = 640;
imageCanvas.height = 480;
var snapBtn = document.createElement("input");
snapBtn.type = "button";
snapBtn.value = "snap";
snapBtn.addEventListener("click", function(){
	selectImage.innerHTML = "";
	console.log("snapBtn.value: " + snapBtn.value);
	if(snapBtn.value == "snap"){
		imageCanvas.getContext("2d").drawImage(imageVideo, 0, 0);
		dataUrl = imageCanvas.toDataURL("image/jpeg");
		console.log("jpeg: " + dataUrl);
		selectImage.innerHTML = "<img src='" + dataUrl + "' width='100%' height='100%'>";
		snapBtn.value = "cancel";
	}else{
		selectImage.appendChild(imageVideo);
		// カメラ起動
		navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || window.navigator.mozGetUserMedia;
		window.URL = window.URL || window.webkitURL;
		var localStream = null;
		navigator.getUserMedia(
			{video: true, audio: false},
			function(stream) { // for success case
				console.log(stream);
				imageVideo.src = window.URL.createObjectURL(stream);
				console.log(imageVideo.src);
			},
			function(err) { // for error case
				console.log(err);
			}
		);
		snapBtn.value = "snap";
	}
});

// 「ファイルから」の設定
fromFileBtn.addEventListener("click", function(){
	fromFileBtn.disabled = true;
	fromCameraBtn.disabled = false;

	$("#imageSelectBtnSpace")[0].innerHTML="";
	$("#imageSelectBtnSpace")[0].appendChild(imageSelectBtn);
});
fromFileBtn.disabled = true;
fromCameraBtn.disabled = false;
$("#imageSelectBtnSpace")[0].appendChild(imageSelectBtn);

// 「カメラから」の設定
fromCameraBtn.addEventListener("click", function(){
	fromCameraBtn.disabled = true;
	fromFileBtn.disabled = false;

	$("#imageSelectBtnSpace")[0].innerHTML="";
	$("#imageSelectBtnSpace")[0].appendChild(snapBtn);
	$("#selectImage")[0].appendChild(imageVideo);

	navigator.getUserMedia = navigator.getUserMedia || navigator.webkitGetUserMedia || window.navigator.mozGetUserMedia;
		window.URL = window.URL || window.webkitURL;
		var localStream = null;
		navigator.getUserMedia(
			{video: true, audio: false},
			function(stream) { // for success case
				console.log(stream);
				imageVideo.src = window.URL.createObjectURL(stream);
			},
			function(err) { // for error case
				console.log(err);
			}
		);
});


// 「モザイクアートの設定」の処理
var divx = 2; // 分割数初期値
var divy = 2; // 分割数初期値
var key = ""; // 検索キーワード初期値
var divxForm = $("#divxForm")[0];
var divyForm = $("#divyForm")[0];
var keyForm = $("#keyForm")[0];
divxForm.value = divx;
divyForm.value = divy;
var defaultButton = $("#defaultButton")[0];
divxForm.addEventListener("change", function(){
	divx = divxForm.value;
});
divyForm.addEventListener("change", function(){
	divy = divyForm.value;
});
keyForm.addEventListener("change", function(){
	key = keyForm.value;
});





















// 「送信」の処理
$('#sendBtn').click(function() {

 if(dataUrl == null){
 	alert("please select image file");
 	return;
 }

	if(window.confirm('この画像を送信していいですか？')){
		console.log("call pushImage");
		console.log("send image data is " + dataUrl);
		console.log("div:" + divx + divy);
		// ぐるぐる開始
		$("#loading").removeClass("hide");

		// API呼び出し
		$.ajax({
			type: 'POST',
			url: endpoint + '/pushImage',
			//分割数とキーワードを引数にする場合
			data: {
				img: dataUrl,
				divx: divx,
				divy: divy,
				key: key
			},
			success: function(tmp){
				console.log("finish pushImage");
				imgId = tmp;
				console.log(imgId);

				$("#imageid")[0].value = imgId;
				$("#mosaic").empty();
				getImage(imgId);
			}
		});
	}
});

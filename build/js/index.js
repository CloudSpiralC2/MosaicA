var endpoint = 'http://ec2-54-64-140-141.ap-northeast-1.compute.amazonaws.com:8080/MosaicA/api';

// init
var imgId;
console.log("js読み込んだよ");

// 「変換する画像を選んでね」の処理
var dataUrl;
var radioButton = $("[name=radioButton]");
var radioGroup = $("#radioGroup")[0];
var selectedImage = $("#selectedImage")[0];
// 「ファイルから」の部品作成
var imageSelectButton = document.createElement("input");
imageSelectButton.type = "file";
imageSelectButton.accept = "image/jpeg";
imageSelectButton.addEventListener("change", function(evt){
	var file = evt.target.files;
	var fReader = new FileReader();
	fReader.readAsDataURL(file[0]);

	// 読み込み終了時
	fReader.onload = function(){

		dataUrl = fReader.result;
		selectedImage.innerHTML = "<img src='" + dataUrl + "' width='100%' height='100%'>";
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
var snapButton = document.createElement("input");
snapButton.type = "button";
snapButton.value = "snap";
snapButton.addEventListener("click", function(){
	selectedImage.innerHTML = "";
	console.log("snapButton.value: " + snapButton.value);
	if(snapButton.value == "snap"){
		imageCanvas.getContext("2d").drawImage(imageVideo, 0, 0);
		dataUrl = imageCanvas.toDataURL("image/jpeg");
		console.log("jpeg: " + dataUrl);
		selectedImage.innerHTML = "<img src='" + dataUrl + "' width='100%' height='100%'>";
		snapButton.value = "cancel";
	}else{
		selectedImage.appendChild(imageVideo);
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
		snapButton.value = "snap";
	}
});
// 「originalImageBoard」の作成
var selectMenu = $("#selectMenu")[0];
selectMenu.appendChild(imageSelectButton);
radioGroup.addEventListener("change", function(){
	selectMenu.innerHTML = "";
	selectedImage.innerHTML = "";
	// 「ファイルから」を選択
	if(radioButton[0].checked){
		console.log(radioButton[0].value);
		selectMenu.appendChild(imageSelectButton);
	}
	// 「カメラ」を選択
	if(radioButton[1].checked){
		console.log(radioButton[1].value);
		selectMenu.appendChild(snapButton);
		snapButton.value = "snap";

		selectedImage.appendChild(imageVideo);
		// カメラ起動
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
	}
});


// 「モザイクアートの設定」の処理
var divx = 2; // 分割数初期値
var divy = 2; // 分割数初期値
var key = ""; // 検索キーワード初期値
var divxForm = $("#divxForm")[0];
var divyForm = $("#divyForm")[0];
var keyForm = $("#keyForm")[0];
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
defaultButton.addEventListener("click", function(){
	var defaultx = 2;
	var defaulty = 2;
	var defaultkey = "";
	divx = defaultx;
	divxForm.value = defaultx;
	divy = defaulty;
	divyForm.value = defaulty;
	key = defaultkey;
	keyForm.value = defaultkey;

});


// 「送信」の処理
$('#send').click(function() {
	// 「ファイルから」の場合
	if(radioButton[0].checked){
		// 何も選択していなければ
		if(imageSelectButton.value.length < 1){
			alert("画像を選択してください");
			return;
		}
	}

	// 使わない？
	//var encodedUrl = encodeURIComponent(dataUrl);

	if(window.confirm('この画像を送信していいですか？')){
		console.log("call pushImage");
		console.log("send image data is " + dataUrl);
		console.log("div:" + divx + divy);
		// API呼び出し
		$.ajax({
			type: 'POST',
			url: endpoint + '/pushImage',
			//data: {img: dataUrl},
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
				// 画面の遷移
				window.open("viewimage.html");
				//location.href="viewimage.html";

				/* データを渡しながら画面遷移する場合 */
				// http://www5e.biglobe.ne.jp/access_r/hp/javascript/js_102.html
				// 転送側
				//window.open("viewimage.html?" + escape(imgId));
				// 受信側
				//	var data = location.search.substring(1, location.search.length);
				//	data = unescape(data);
			}
		});
	}
});
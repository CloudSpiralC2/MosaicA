var endpoint = "http://52.68.162.198:8080/MosaicA/api";

var dataUrl; // モザイク画像生成用変数
var isMobile=0; // モバイル端末フラグ
var result_original_width = 0; // モザイク画像表示用横サイズ
var result_original_height = 0; // 縦サイズ

// 読み込み完了時処理
$(function(){

  // スマホ表示
  var useragent = navigator.userAgent;
  if(useragent.indexOf('iPhone') > 0 || useragent.indexOf('iPod') > 0 ||
    useragent.indexOf('Android') > 0){
     isMobile = 1;
     $("#left_div").removeClass('horizontal');
     $("#left_div").addClass('vertical');
     $('head').append('<link rel="stylesheet" href="css/index_mob.css"></link>');
  }
  // 及び一般
  else{
    $('head').append('<link rel="stylesheet" href="css/index.css"></link>');
  }

  // 説明表示
  $("#help").on('click', function(e){
    $("#dialog").toggle();
  });
  // プレースホルダ画像のdataUrl取得 TODO
  /*
  var source = $("#original_image")[0];
  var image = new Image();
  image.src = source.src;

  var canvas = document.createElement('canvas');
  canvas.width = image.width;
  canvas.height = image.height;
  // var context =
  canvas.getContext("2d").drawImage(source, 0, 0);
  dataUrl = canvas.toDataURL('image/jpeg');
  */

  // ファイルから
  $("#file").on('click', function(e){
    var input = $('input[type="file"]')[0];
    // ボタンクリック時は隠してあるinputをクリックした扱いにする
    if(isMobile==1){
      e.preventDefault();
     console.log("mob");
      input.click();
    }
    else{
      input.click();
    }
  });

  // ファイル選択時の処理
  $('input[type="file"]').change(function(){
    var file = $(this).prop('files')[0];
    // jpegじゃないとき
    if(!file.type.match('image/jpeg')){
      alert("Please choose JPEG file.");
      return;
    }

    // 画像表示
    var reader = new FileReader();
    reader.onload = function() {
      dataUrl = reader.result;
      $("#original_image").attr('src', dataUrl);
    }
    reader.readAsDataURL(file);
  });

  // カメラから
  $("#camera").on('click', function(){
    // カメラ使えるかチェック
    if(!(navigator.getUserMedia || navigator.webkitGetUserMedia || navigator.mozGetUserMedia || navigator.msGetUserMedia)){
      alert("カメラが使えません");
      return;
    }

    // カメラ映像をストリーミング取得
     var video = $("#video")[0]; // getElementByIdと同等
    //var video = document.createElement('video');
    // サイズの設定
    video.width = $("#original_image").css("width").split("p")[0]; // 取得したサイズからpxを除いて設定
    video.height = $("#original_image").css("height").split("p")[0];
    video.autoplay = 1;

    // 現在の表示画像を消す
    $("#original_image").attr('hidden', 'true');;
    // ビデオ表示
    $("#video").removeAttr('hidden');

    var localMediaStream = null;

    window.URL = window.URL || window.webkitURL;
    // ブラウザ間の相違を吸収する
    navigator.mediaDevices = navigator.mediaDevices || ((navigator.mozGetUserMedia || navigator.webkitGetUserMedia) ? {
      getUserMedia: function(c) {
        return new Promise(function(y, n) {
          (navigator.mozGetUserMedia ||
            navigator.webkitGetUserMedia).call(navigator, c, y, n);
          });
        }
      } : null);
    var constraints = {video: true, audio: false};
    // video許可をとり，映像を流す
    navigator.mediaDevices.getUserMedia(constraints)
      .then(function(stream) {
        video.src = window.URL.createObjectURL(stream);
        localMediaStream = stream;
      })
      .catch(function(err){
        alert("camera error: 許可がありません" + err);
      });

    // canvasの設定
    var canvas = document.createElement('canvas');
    canvas.width = video.width;
    canvas.height = video.height;
    var context = canvas.getContext('2d');

    // canvasに描画して画像にする
    var snapshot = function(){
      if(localMediaStream){
        context.drawImage(video, 0, 0);//, video.width, video.height);
        var canvas_dataurl = canvas.toDataURL('image/jpeg');
        $("#original_image").attr('src', canvas_dataurl);
        $("#video").attr('hidden', 'true');
        $("#original_image").removeAttr('hidden');
        dataUrl = canvas_dataurl;
      }
    }

    // カメラ画像をクリックで
    $("#video").on('click', function(){
      snapshot();
    });
  });

  // モザイク処理実行
  $("#mosaic_button").on('click', function(){
    console.log(dataUrl);
    if(dataUrl == null){
      alert("画像を選択してください");
      return;
    }
    var divx = $("#divx").val();
    var divy = $("#divy").val();
    var key = $("#key").val();
    if(key==null) key = "";
    if (confirm("画像を送信してもよろしいですか？")){
      $("#loading").attr('active', 'true');

      // Notificationを取得
      var Notification = window.Notification || window.mozNotification || window.webkitNotification;
      Notification.requestPermission(function(permission){;});

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
          imgId = tmp;
          console.log(imgId);

          $("#imageid")[0].value = imgId;
          getImage(imgId);

          // デスクトップ通知
          var notif = new Notification('MosaicA', {
          	body: 'MosaicA has finished creating your mosaic art!!',
          	icon: 'image/favicon.ico'
          });
        }
      });
    }
  });

  // モザイクアート取得処理
  // Mosaicボタン押下時の処理
  $("#getimage").on('click', function() {
    var id = $("#imageid").val();
    // imageid判定
    if(id == "" || id == 0){
      alert("Please enter image ID")
      return;
    }
    getImage(id);
  });

  // savaImage処理
  $("#save_mosaic_image").on('click', function(){
    var spinner = $("#loading2");
    var imageid = $("#imageid").val();
    spinner.attr('active', 'true');
    $("#img_link").remove();
    $.ajax({
      url: endpoint + "/saveImage",
      data: {
        imageId: imageid
      },
      dataType: "text",
      crossDomain: true,
      success: function(img_str){
        console.log(img_str);
        spinner.removeAttr('active');
        $("body").append('<a href="' + img_str + '" download="' + imageid + '.jpg" id="img_link"></a>');
        $("#img_link")[0].click();
      },
      error: function(){
        spinner.removeAttr('active');
        alert("Image ID を確認してください");
      }
    });
  });

  $("#show_original_size").on('click', function(){
    window.open("viewimage.html?" + $("#imageid").val());
  });

  //tweetボタン押下時
  $("#tweet").on('click', function(){
    var imageId = $("#imageid").val();
    if(imageId==0 || imageId==""){
      alert("Image IDを入力してください")
      return;
    }
    var url = endpoint + "/twitterRequest?imageId=" + imageId;
    window.open(url);
  });
});
// ここまで読み込み時処理

// モザイクアート取得関数
function getImage(imageid){
  var urllist = [];
  $("#loading").attr('active', 'true');

  $.ajax({
    url : endpoint + "/getImage",
    data : {
      imageId : imageid
    },
    dataType : "xml",
    crossDomain : true,
    success : function(xml) {
      // 存在しない or 処理中のときはreturn
      if($(xml).find('status').text()=="processing"){
	      $("#loading").removeAttr('active');
        return;
      }
      $($(xml).find('child')).each(function() {
        var str = $(this).find('url').text();
        urllist.push(str);
        var x = $(this).find('x').text();
        var y = $(this).find('y').text();
        //console.log("x:" + x + "y" + y);
      });
      var divx = $(xml).find('divx').text(); // 横方向分割数
      var divy = $(xml).find('divy').text(); // 縦方向分割数
      var sizex = $(xml).find('sizex').text(); // 横サイズ
      var sizey = $(xml).find('sizey').text(); // 縦サイズ

      // 結果表示画像のサイズ 縦長と横長を繰り返し取得してもサイズが変わらないようにする
      var imagebox = $("#result_image_box");
      tmp_width = imagebox.css('width').split("p")[0];
      tmp_height = imagebox.css('height').split("p")[0];
      if(result_original_width == 0){
        result_original_width = tmp_width;
      }
      if(result_original_height == 0){
        result_original_height = tmp_height;
      }

      // 横長画像の場合
      if(parseInt(sizex) >= parseInt(sizey)){
        var ratiox = result_original_width / sizex;
        var child_sizex = sizex / divx * ratiox //* 0.9 ; // 子の横サイズ
        var child_sizey = sizey / divy * ratiox; // 子の縦サイズ
      }
      // 縦長画像の場合
      else{
        var ratioy = result_original_height / sizey;
        var child_sizex = sizex / divx * ratioy ; // 子の横サイズ
        var child_sizey = sizey / divy * ratioy; // 子の縦サイズ
      }
      lineUpImage(divx, child_sizex, child_sizey, urllist);
    }
  });
}

// 一列の画像の数，一つの画像の横幅，一つの画像の縦幅，画像のリスト
function lineUpImage(divX, width, height, children) {
  $("#result_image_box").empty();
	count = 0;
	line = 1;
	for (var i = 0; i < children.length; i++) {
		if (children[i] == "") {
			$("#result_image_box").append('<img src="image/gray.jpg" style="width:' + width + 'px; height:' + height + 'px;" class="mosaic">');
		} else {
			$("#result_image_box").append('<img src="' + children[i] + '" style="width:' + width + 'px; height:' + height + 'px;" class="mosaic">');
  	}
		count++;
		if (count >= divX) {
			$("#result_image_box").append('<br>');
			count = 0;
			line++;
		}
	}
	// ぐるぐる終わり
	$("#loading").removeAttr('active');
}

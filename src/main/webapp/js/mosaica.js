var endpoint = "http://52.69.136.65:8080/MosaicA/api";

var dataUrl;

// 読み込み完了時処理
$(function(){

  // プレースホルダ画像のdataUrl取得 TODO
  /*
  var source = $("#original_image").attr("src");
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
  $("#file").on('click', function(){
    // ボタンクリック時は隠してあるinputをクリックした扱いにする
    $('input[type="file"]').click();
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

  // モザイク処理実行
  $("#mosaic_button").on('click', function(){
    console.log(dataUrl);
    var divx = $("#divx").val();
    var divy = $("#divy").val();
    var key = $("#key").val();
    if(key==null) key = "";
    console.log(divx);
    console.log(divy);
    console.log(key);
    if (confirm("画像を送信してもよろしいですか？")){
      $("#loading").attr('active', 'true');
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
          getImage(imgId);
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
      var ratiox = 500 / sizex;
      var ratioy = 500 / sizey;
      var child_sizex = (sizex / divx) * ratiox ; // 子の横サイズ
      var child_sizey = sizey / divy * ratiox; // 子の縦サイズ
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
			$("#result_image_box").append('<img src="image/gray.jpg" style="width:' + width + 'px; height:' + height + 'px;">');
		} else {
			$("#result_image_box").append('<img src="' + children[i] + '" style="width:' + width + 'px; height:' + height + 'px;">');
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

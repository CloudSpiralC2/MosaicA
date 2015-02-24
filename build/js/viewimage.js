var endpoint = 'http://ec2-54-92-0-242.ap-northeast-1.compute.amazonaws.com:8080/MosaicA/api';

// 読み込み完了時処理
$(function() {
	$("#getimage").on('click', function() {
		var id = $("#imageid").val();
		$("#mosaic").empty();
		// ぐるぐる開始
		$("#loading").removeClass("hide");
		getImage(id);
	});
});

// 関数
function getImage(imageid) {
	var urllist = [];

	$.ajax({
		url : endpoint + "/getImage",
		data : {
			imageId : imageid
		},
		dataType : "xml",
		crossDomain : true,
		success : function(xml) {
			$($(xml).find('child')).each(function() {
				var str = $(this).find('url').text();
				urllist.push(str);
				var x = $(this).find('x').text();
				var y = $(this).find('y').text();
				//console.log("x:" + x + "y" + y);
			});
			var divx = $(xml).find('divx').text();
			var divy = $(xml).find('divy').text();
			var sizex = $(xml).find('sizex').text() / divx;
			var sizey = $(xml).find('sizey').text() / divy;
			lineUpImage(divx, sizex, sizey, urllist);
		}
	});
}

/*
// 一列の画像の数，一つの画像の横幅，一つの画像の縦幅，画像のリスト
function lineUpImage(divX, width, height, children) {
	count = 0;
	line = 1;
	for (var i = 0; i < children.length; i++) {
		if (children[i] == "") {
			$("#mosaic").append('<img src="image/gray.jpg" style="width:' + width + 'px; height:' + height + 'px;position:absolute;top:' + line * height + 'px;left:' + count * width + 'px;">');
		} else {
			$("#mosaic").append('<img src="' + children[i] + '" style="width:' + width + 'px; height:' + height + 'px;position:absolute;top:' + line * height + 'px;left:' + count * width + 'px;">');
		}
		count++;
		if (count >= divX) {
			$("#mosaic").append('<br>');
			count = 0;
			line++;
		}
	}
	// ぐるぐる終わり
	$("#loading").addClass("hide");
	//$("#mosaic").append('<button class="btn btn-lg" onclick="alert("save!!");"><span class="lg-send">Save</span><span class="lg-sending"><i class="fa fa-fw fa-spinner fa-spin"></i>&nbsp;Subscribing</span></button>');
}*/

// 一列の画像の数，一つの画像の横幅，一つの画像の縦幅，画像のリスト
function lineUpImage(divX, width, height, children) {
	count = 0;
	line = 1;
	for (var i = 0; i < children.length; i++) {
		if (children[i] == "") {
			$("#mosaic").append('<img src="image/gray.jpg" style="width:' + width + 'px; height:' + height + 'px;">');
		} else {
			$("#mosaic").append('<img src="' + children[i] + '" style="width:' + width + 'px; height:' + height + 'px;">');
		}
		count++;
		if (count >= divX) {
			$("#mosaic").append('<br>');
			count = 0;
			line++;
		}
	}
	// ぐるぐる終わり
	$("#loading").addClass("hide");
	//$("#mosaic").append('<button class="btn btn-lg" onclick="alert("save!!");"><span class="lg-send">Save</span><span class="lg-sending"><i class="fa fa-fw fa-spinner fa-spin"></i>&nbsp;Subscribing</span></button>');
}
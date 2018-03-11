
$(document).ready(function(){
	
	function makeXMLHttpRequestObject() {
		var resObj = null;
		try {
			resObj = new ActiveXObject("Microsoft.XMLHTTP");
		} catch (Error) {
			try {
				resObj = new ActiveXObject("MSXML2.XMLHTTP");
			} catch (Error) {
				try {
					resObj = new XMLHttpRequest();
				} catch (Error) {
					alert("Error creating HttpRequestObject!");
				}
			}
		}
		return resObj;
	}
	
	//var query = getQueryParams(document.location.search);
	//alert(query.foo);
	function getQueryParams(qs) {
		qs = qs.split('+').join(' ');

		var params = {},
			tokens,
			re = /[?&]?([^=]+)=([^&]*)/g;

		while (tokens = re.exec(qs)) {
			params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
		}

		return params;
	}
	
	//need .on() instead of .mouseover() since these elements could have been dynamically created
	//also need to specify selector for on-function since elements could have been dynamically created
	$(".window").on("mouseover", ".button-area img", function() {
		var src = $(this).attr("src");
		if (src.indexOf("_highlight.png") == -1) {  //if doesnt already contain
			$(this).attr("src", src.replace(".png", "_highlight.png"));
		}
	});
	$(".window").on("mouseout", ".button-area img", function() {
		var src = $(this).attr("src");
		$(this).attr("src", src.replace("_highlight.png", ".png"));
	});
	
	
	function show(accgen) {
		$("#accounts").empty();
		$("#accounts").append('<img src="./assets/images/loading.gif" />');
		
		var table = '<div class="mbr-article-custom" style="overflow:auto">'
				+ '<table name="accTable" id="accTable" class="accTable sortable">'
		 		+ '<tr>'
				+ '<td class="tableTitle"><b>Nickname</b></td>'
				+ '<td class="tableTitle"><b>Email</b></td>'
				+ '<td class="tableTitle"><b>Password</b></td>'
				+ '<td class="tableTitle"><b>Script</b></td>'
				+ '<td class="tableTitle"><b>Proxy:Port</b></td>'
				+ '<td class="tableTitle"><b>World</b></td>'
				+ '<td class="tableTitle"><b>Params</b></td>'
				+ '<td class="tableTitle"><b>Max <br />minutes <br />running</b></td>'
				+ '<td class="tableTitle"><b>Current <br />minutes <br />running</b></td>'
				+ '<td class="tableTitle"><b>Last 24h reset</b></td>'
				+ '<td class="tableTitle"><b>Max <br />Chunk <br />minutes</b></td>'
				+ '<td class="tableTitle"><b>Current <br />chunk <br />minutes</b></td>'
				+ '<td class="tableTitle"><b>Max <br />Break <br />minutes</b></td>'
				+ '<td class="tableTitle"><b>Current <br />break <br />minutes</b></td>'
				+ '<td class="tableTitle"><b>Acknowledged <br />start</b></td>'
				+ '<td class="tableTitle"><b>PID</b></td>'
				+ '<td class="tableTitle"><button row="-1" column="501">Start all</button>'
				+ '<td class="tableTitle"><button row="-1" column="502">Kill all</button>'
				+ '</tr>';
		var params = "dreamling=" + dreamling;
		var http = makeXMLHttpRequestObject();
		var url = "/data";
		http.onreadystatechange = function() {
			if (this.readyState == 4) { //java responses with generated table-data
				if (http.responseText) {
					var accs = http.responseText.split("!");
					for (h = 0; h < accs.length; h++) {
						var data = accs[h].split("~");
						if (data.length >= 16) {
							table += '<tr>';
							for (i = 0; i < 16; i++) {
								switch (i) { // only some cells are editable
								case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 10: case 12:
									table = table + '<td class="tableCell"><a href="#" row="' + h + '" column="' + i + '" class="editableField">' + data[i] + "</a></td>";
									break;
								default:
									table = table + '<td class="tableCell">' + data[i] + "</td>";
								}
							}
							table = table + '<td class="tableCell"><button row="' + h + '" column="111">Start</button></td>';
							table = table + '<td class="tableCell"><button row="' + h + '" column="222">Kill</button></td>';
							table = table + '<td class="tableCell"><button row="' + h + '" column="333">Delete</button></td>';
							table += "</tr>";
						}
					}
					table += "</table></div>";
					$("#accounts").empty();
					$("#accounts").append('<button row="-1" column="444">Add Account</button><br />');
					$("#accounts").append(table);
					sorttable.makeSortable(document.getElementById("accTable"));
					$(".editableField").each(function() {
						var row = $(this).attr("row");
						var column = $(this).attr("column");
						$(this).editable({
						    type: 'text',
							mode: 'inline',
							pk: row + ":" + column + ":" + dreamling,
							url: './data'
						});
					});
					$("button").click(function() {
						$("#accounts").empty();
						$("#accounts").append('<img src="./assets/images/loading.gif" />');
						var pk = $(this).attr("row") + ":" + $(this).attr("column") + ":" + dreamling;
						var params2 = "pk=" + pk + "&value=0";
						var http2 = makeXMLHttpRequestObject();
						http2.onreadystatechange = function() {
							if (this.readyState == 4) {
								show(dreamling);
							}
						}
						http2.open("POST", "/data", true);
						http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
						http2.send(params2);
					});
				}
			}
		}
		http.open("GET", url + "?" + params, true);
		http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		http.send(null);
	}
	
	function selectSidebar(selected) {
		$(".sidebar-button").each(function() {
			$(this).css("border-right", "10px solid #fff");
			$(this).css("background-color", "transparent");
			$(this).hover(function() {
				$(this).css("border-right", "10px solid #38D");
				$(this).css("background-color", "rgb(110, 110, 110)");
			}, function() {
				$(this).css("border-right", "10px solid #fff");
				$(this).css("background-color", "transparent");
			});
		});
		selected.css("border-right", "10px solid #38D");
		selected.css("background-color", "rgb(100, 130, 150)");
		selected.hover(function() {
			$(this).css("border-right", "10px solid #38D");
			$(this).css("background-color", "rgb(100, 130, 150)");
		}, function() {
			$(this).css("border-right", "10px solid #38D");
			$(this).css("background-color", "rgb(100, 130, 150)");
		});
	}
	
	$(".sidebar-button").click(function() {
		var id = $(this).attr("id");
		selectSidebar($(this));
		if (id === "uploadAccounts") {
			$("#CONTENT_SECTION").children().hide();
			$("#uploadSection").show();
		} else if (id === "viewAccounts") {
			$("#CONTENT_SECTION").children().hide();
			$("#dreamlist").show();
		} else {
			show(id);
		}
	});
	
	selectSidebar($("#viewAccounts"));
	
	$("#reload").on("click", function() {
		var query = getQueryParams(document.location.search);
		var id = "id=" + query.id;
		var url = window.location.href.split('?')[0];
		
		window.location.href = url + "?" + id;
	});
	
	
});
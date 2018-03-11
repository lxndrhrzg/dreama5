
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
	
	$(".window").on("click", ".deleteEntry", function() {
		deleteEntry(this);
	});
	
	function reload() {
		var query = getQueryParams(document.location.search);
		var id = "id=" + query.id;
		var url = window.location.href.split('?')[0];
		
		window.location.href = url + "?" + id;
	}
	
	function updateHiding() {
		if ($("#hideBreakBox")[0].checked) {
			$(".hideable1").hide();
			for (i = 7; i < 14; i++) {
				$("td[column=" + i + "]").each(function() {
					$(this).hide();
				});
			}
		} else {
			$(".hideable1").show();
			for (i = 7; i < 14; i++) {
				$("td[column=" + i + "]").each(function() {
					$(this).show();
				});
			}
		}
	}
	
	$("#hideBreakBox").click(function () {
		updateHiding();
	});
	
	$("#hideBreakBox")[0].checked = true; //default activated
	
	function show(ids) {
		$("#accounts").append('<img id="loading" src="./assets/images/loading.gif" />');
		
		var params = "dreamling=" + ids;
		var http = makeXMLHttpRequestObject();
		var url = "/data";
		http.onreadystatechange = function() {
			if (this.readyState == 4) { //java responses with generated table-data
				if (http.responseText || http.responseText === "") {
					$("#loading").remove();
					var dreamlings = http.responseText.split("DREAMLING_SEPARATOR");
					for (num = 0; num < dreamlings.length; num++) {
						var dreamling = ids.split("DREAMLING_SEPARATOR")[num];
						var accs = dreamlings[num].split("!");
						var table = '<div class="mbr-article-custom" style="overflow:auto">'
								+ '<table id="accTable' + dreamling.replace(" ", "_") + '" class="accTable sortable" accCount="' + (accs.length - 1) + '" dreamling="' + dreamling + '">'
								+ '<tr>'
								+ '<td class="tableTitle"><b>Nickname</b></td>'
								+ '<td class="tableTitle"><b>Email</b></td>'
								+ '<td class="tableTitle"><b>Password</b></td>'
								+ '<td class="tableTitle"><b>Script</b></td>'
								+ '<td class="tableTitle"><b>Proxy:Port</b></td>'
								+ '<td class="tableTitle"><b>World</b></td>'
								+ '<td class="tableTitle"><b>Params</b></td>'
								+ '<td class="tableTitle hideable1"><b>Max <br />minutes <br />running</b></td>'
								+ '<td class="tableTitle hideable1"><b>Current <br />minutes <br />running</b></td>'
								+ '<td class="tableTitle hideable1"><b>Last 24h reset</b></td>'
								+ '<td class="tableTitle hideable1"><b>Max <br />Chunk <br />minutes</b></td>'
								+ '<td class="tableTitle hideable1"><b>Current <br />chunk <br />minutes</b></td>'
								+ '<td class="tableTitle hideable1"><b>Max <br />Break <br />minutes</b></td>'
								+ '<td class="tableTitle hideable1"><b>Current <br />break <br />minutes</b></td>'
								+ '<td class="tableTitle"><b>Acknowledged <br />start</b></td>'
								+ '<td class="tableTitle"><b>PID</b></td>'
								+ '<td class="tableTitle"><b>State</b></td>'
								+ '<td class="tableTitle"><b>Minutes<br />in state</b></td>'
								+ '<td class="tableTitle"><button class="dreamlingButton" row="-1" column="501" dreamling="' + dreamling + '">Start all</button>'
								+ '<td class="tableTitle"><button class="dreamlingButton" row="-1" column="502" dreamling="' + dreamling + '">Kill all</button>'
								+ '<td class="tableTitle"><b></b></td>'
								+ '</tr>';
						if (accs.length >= 1) {
							var titles = accs[0].split("~");
							var table = '<div class="mbr-article-custom" style="overflow:auto">'
								+ '<table id="accTable' + dreamling.replace(" ", "_") + '" class="accTable sortable" accCount="' + (accs.length - 1) + '" dreamling="' + dreamling + '">'
								+ '<tr>'
							for (i = 0; i < titles.length; i++) {
								table += '<td class="tableTitle"><b>' + titles[i] + '</b></td>';
							}
							table += '<td class="tableTitle"><button class="dreamlingButton" row="-1" column="501" dreamling="' + dreamling + '">Start all</button>'
								+ '<td class="tableTitle"><button class="dreamlingButton" row="-1" column="502" dreamling="' + dreamling + '">Kill all</button>'
								+ '<td class="tableTitle"><b></b></td>'
								+ '</tr>';
						}
						for (h = 0; h < accs.length; h++) {
							var data = accs[h].split("~");
							if (data.length >= 18) {
								table += '<tr>';
								for (i = 0; i < 18; i++) {
									switch (i) { // only some cells are editable
									case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7: case 10: case 12:
										table = table + '<td class="tableCell" column="' + i + '"><a href="#" row="' + h + '" column="' + i + '" dreamling="' + dreamling + '" class="editableField">' + data[i] + "</a></td>";
										break;
									default:
										table = table + '<td class="tableCell" column="' + i + '">' + data[i] + "</td>";
									}
								}
								table = table + '<td class="tableCell"><button class="dreamlingButton" row="' + h + '" column="111" dreamling="' + dreamling + '">Start</button></td>';
								table = table + '<td class="tableCell"><button class="dreamlingButton" row="' + h + '" column="222" dreamling="' + dreamling + '">Kill</button></td>';
								table = table + '<td class="tableCell"><button class="dreamlingButton" row="' + h + '" column="333" dreamling="' + dreamling + '">Delete</button></td>';
								table = table + '<td class="tableCell"><button class="replaceButton" row="' + h + '" dreamling="' + dreamling + '">Replace</button></td>';
								table += "</tr>";
							} else if (data.length == 1) {
								table += '<tr><td>' + data[0] + '</td></tr>';
							}
						}
						table += "</table></div>";
						$("#loading" + dreamling.replace(" ", "_")).remove();
						$("#accounts").append('<div class="centerText"><span class="contentTitle">Dreamling: ' + dreamling + '</span></div><br />');
						$("#accounts").append('<button class="renameButton" dreamling="' + dreamling + '">Rename Dreamling</button>');
						$("#accounts").append('<button class="dreamlingButton" row="-1" column="444" dreamling="' + dreamling + '">Add Account</button>');
						$("#accounts").append('<button class="nextAccounts" dreamling="' + dreamling + '">Paste next Accounts</button>');
						$("#accounts").append('<button class="nextProxies" dreamling="' + dreamling + '">Paste next Proxies</button>');
						$("#accounts").append(table + "<br />");
						sorttable.makeSortable(document.getElementById("accTable" + dreamling.replace(" ", "_")));
						updateHiding();
						if ($("#allCount").length) { //if exists
							var count = parseInt($("#allCount").text(), 10);
							$("#allCount").empty();
							$("#allCount").append(count + accs.length);
						}
						if ($("#bannedCount").length) { //if exists
							var count = parseInt($("#bannedCount").text(), 10);
							$("#accTable" + dreamling.replace(" ", "_") + ' td.tableCell[column=16]:contains("BANNED")').each(function() {
								count++;
							});
							$("#accTable" + dreamling.replace(" ", "_") + ' td.tableCell[column=16]:contains("LOCKED")').each(function() {
								count++;
							});
							$("#bannedCount").empty();
							$("#bannedCount").append(count);
						}
						$(".editableField").each(function() {
							var row = $(this).attr("row");
							var column = $(this).attr("column");
							var dreamling = $(this).attr("dreamling");
							$(this).editable({
								type: 'text',
								mode: 'inline',
								pk: row + ":" + column + ":" + dreamling,
								url: './data'
							});
						});
						$(".renameButton").off("click");
						$(".renameButton").click(function() {
							var dreamling = $(this).attr("dreamling");
							showPrompt("Rename Dreamling", function(newName) {
								$("#accounts").empty();
								$("#accounts").append('<img src="./assets/images/loading.gif" />');
								var params2 = "dreamling=" + dreamling + "&newName=" + newName;
								var http2 = makeXMLHttpRequestObject();
								http2.onreadystatechange = function() {
									if (this.readyState == 4) {
										reload();
									}
								}
								http2.open("POST", "/data", true);
								http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
								http2.send(params2);
							});
						});
						$(".dreamlingButton").off("click");
						$(".dreamlingButton").click(function() {
							$("#accounts").empty();
							$("#accounts").append('<img src="./assets/images/loading.gif" />');
							var dreamling = $(this).attr("dreamling");
							var pk = $(this).attr("row") + ":" + $(this).attr("column") + ":" + dreamling;
							var params2 = "pk=" + pk + "&value=0";
							var http2 = makeXMLHttpRequestObject();
							http2.onreadystatechange = function() {
								if (this.readyState == 4) {
									showOnly(dreamling);
								}
							}
							http2.open("POST", "/data", true);
							http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
							http2.send(params2);
						});
						$(".nextAccounts").off("click");
						$(".nextAccounts").click(function() {
							var dreamling = $(this).attr("dreamling");
							showPrompt("Paste Accounts", function(accs) {
								//alert("input was: \n" + accs);
								$("#accounts").empty();
								$("#accounts").append('<img src="./assets/images/loading.gif" />');
								var params2 = "nextAccs=" + accs + "&dreamling=" + dreamling;
								var http2 = makeXMLHttpRequestObject();
								http2.onreadystatechange = function() {
									if (this.readyState == 4) {
										showOnly(dreamling);
									}
								}
								http2.open("POST", "/data", true);
								http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
								http2.send(params2);
							});
						});
						$(".nextProxies").off("click");
						$(".nextProxies").click(function() {
							var dreamling = $(this).attr("dreamling");
							showPrompt("Paste Proxies", function(proxies) {
								//alert("input was: \n" + proxies);
								$("#accounts").empty();
								$("#accounts").append('<img src="./assets/images/loading.gif" />');
								var params2 = "nextProxies=" + proxies + "&dreamling=" + dreamling;
								var http2 = makeXMLHttpRequestObject();
								http2.onreadystatechange = function() {
									if (this.readyState == 4) {
										showOnly(dreamling);
									}
								}
								http2.open("POST", "/data", true);
								http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
								http2.send(params2);
							});
						});
						$(".replaceButton").off("click");
						$(".replaceButton").click(function() {
							var row = $(this).attr("row");
							var dreamling = $(this).attr("dreamling");
							showPrompt("Paste one Account credential line", function(line) {
								$("#accounts").empty();
								$("#accounts").append('<img src="./assets/images/loading.gif" />');
								var params2 = "replaceAcc=" + row + "&credentials=" + line + "&dreamling=" + dreamling;
								var http2 = makeXMLHttpRequestObject();
								http2.onreadystatechange = function() {
									if (this.readyState == 4) {
										showOnly(dreamling);
									}
								}
								http2.open("POST", "/data", true);
								http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
								http2.send(params2);
							});
						});
					}
					
				}
			}
		}
		http.open("GET", url + "?" + params, true);
		http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
		http.send(null);
	}
	
	function showOnly(dreamling) {
		$("#accounts").empty();
		show(dreamling);
	}
	
	function initDynamicButtons() {
		
		$("#replaceBanned").click(function() {
			showPrompt("Paste Accounts", function(accs) {
				var accArray = accs.split("\n");
				var pos = 0;
				var connections = [];
				$(".accTable").each(function() {
					var dreamling = $(this).attr("dreamling");
					var banned = $(this).find('td.tableCell[column=16]:contains("BANNED")').length
									+ $(this).find('td.tableCell[column=16]:contains("LOCKED")').length;
					if (pos + banned > accArray.length) { //if not enough accs to replace remaining banned accounts
						banned = accArray.length - pos;  //only replace as many as possible
					}
					if (banned > 0) {
						var accsForThisDreamling = ""; //appending right amount of accounts to this string
						for (i = 0; i < banned; i++) {
							if (i === banned - 1) { //no linebreak for last acc
								accsForThisDreamling = accsForThisDreamling + accArray[pos + i];
							} else {
								accsForThisDreamling = accsForThisDreamling + accArray[pos + i] + "\n";
							}
						}
						pos += banned; //not sure if -1 is necessary here
						
						var params2 = "replaceBanned=" + accsForThisDreamling + "&dreamling=" + dreamling;
						var connection = $.ajax({
							url: "/data",
							type: "POST",
							contentType: "application/x-www-form-urlencoded",
							data: params2
						});
						connections.push(connection);
					}
				});
				$("#accounts").empty();
				$("#accounts").append('<img src="./assets/images/loading.gif" />');
				$.when.apply($, connections).then(function() {
					$("#showAll").click();
				});
			});
		});
		
		$("#replaceAllAcc").click(function() {
			showPrompt("Paste Accounts", function(accs) {
				var accArray = accs.split("\n");
				var pos = 0;
				var connections = [];
				$(".accTable").each(function() {
					var dreamling = $(this).attr("dreamling");
					var count = $(this).find('td.tableCell[column=3]:not(:contains("Mule"))').length;
					if (pos + count > accArray.length) { //if not enough accs to replace
						count = accArray.length - pos;  //only replace as many as possible
					}
					if (count > 0) {
						var accsForThisDreamling = ""; //appending right amount of accounts to this string
						for (i = 0; i < count; i++) {
							if (i === count - 1) { //no linebreak for last acc
								accsForThisDreamling = accsForThisDreamling + accArray[pos + i];
							} else {
								accsForThisDreamling = accsForThisDreamling + accArray[pos + i] + "\n";
							}
						}
						pos += count; //not sure if -1 is necessary here (seems not to be)
						
						var params2 = "nextAccs=" + accsForThisDreamling + "&dreamling=" + dreamling;
						var connection = $.ajax({
							url: "/data",
							type: "POST",
							contentType: "application/x-www-form-urlencoded",
							data: params2
						});
						connections.push(connection);
					}
				});
				$("#accounts").empty();
				$("#accounts").append('<img src="./assets/images/loading.gif" />');
				$.when.apply($, connections).then(function() {
					$("#showAll").click();
				});
			});
		});
		
		$("#replaceAllProxies").click(function() {  //this is copypasted from replaceAllAcc so variable names are for accs although its proxies
			showPrompt("Paste Proxies", function(accs) {
				var accArray = accs.split("\n");
				var pos = 0;
				var connections = [];
				$(".accTable").each(function() {
					var dreamling = $(this).attr("dreamling");
					var count = $(this).find('td.tableCell[column=3]:not(:contains("Mule"))').length;
					if (pos + count > accArray.length) { //if not enough accs to replace
						count = accArray.length - pos;  //only replace as many as possible
					}
					if (count > 0) {
						var accsForThisDreamling = ""; //appending right amount of accounts to this string
						for (i = 0; i < count; i++) {
							if (i === count - 1) { //no linebreak for last acc
								accsForThisDreamling = accsForThisDreamling + accArray[pos + i];
							} else {
								accsForThisDreamling = accsForThisDreamling + accArray[pos + i] + "\n";
							}
						}
						pos += count; //not sure if -1 is necessary here (seems not to be)
						
						var params2 = "nextProxies=" + accsForThisDreamling + "&dreamling=" + dreamling;
						var connection = $.ajax({
							url: "/data",
							type: "POST",
							contentType: "application/x-www-form-urlencoded",
							data: params2
						});
						connections.push(connection);
					}
				});
				$("#accounts").empty();
				$("#accounts").append('<img src="./assets/images/loading.gif" />');
				$.when.apply($, connections).then(function() {
					$("#showAll").click();
				});
			});
		});
		
		$("#startLauncher").click(function() {
			var connections = [];
			$(".accTable").each(function() {
				var dreamling = $(this).attr("dreamling");
				var params2 = "startLauncher=startLauncher&dreamling=" + dreamling;
				var connection = $.ajax({
					url: "/data",
					type: "POST",
					contentType: "application/x-www-form-urlencoded",
					data: params2
				});
				connections.push(connection);
			});
			$("#accounts").empty();
			$("#accounts").append('<img src="./assets/images/loading.gif" />');
			$.when.apply($, connections).then(function() {
				$("#showAll").click();
			});
		});
		
		$("#replaceXY").click(function() {
			showDoublePrompt("Replace X with Y in ALL Attributes", function(X, Y) {
				var connections = [];
				$(".accTable").each(function() {
					var dreamling = $(this).attr("dreamling");
					var params = "replaceXY=" + dreamling + "&X=" + X + "&Y=" + Y;
					
					var connection = $.ajax({
						url: "/data",
						type: "POST",
						contentType: "application/x-www-form-urlencoded",
						data: params
					});
					connections.push(connection);
				});
				$("#accounts").empty();
				$("#accounts").append('<img src="./assets/images/loading.gif" />');
				$.when.apply($, connections).then(function() {
					$("#showAll").click();
				});
			});
		});
	}
	
	function showPrompt(title, callback) {
		var result = "";
		$("#dialog").empty();
		$("#dialog").append('<textarea id="inputBox"></textarea>');
        $("#dialog").dialog({
			title: title,
            autoOpen: false,
            resizable: true,
            modal: true,
            buttons: {
                "Submit": function() {
					var result = $("#inputBox").val();
                    $(this).dialog("close");
					callback(result);
                }
            }
        });
		$("#dialog").dialog("open");
	}
	
	function showDoublePrompt(title, callback) {
		var result = "";
		$("#dialog").empty();
		$("#dialog").append('<textarea id="inputBox1"></textarea><br/>');
		$("#dialog").append('<textarea id="inputBox2"></textarea>');
        $("#dialog").dialog({
			title: title,
            autoOpen: false,
            resizable: true,
            modal: true,
            buttons: {
                "Submit": function() {
					var result1 = $("#inputBox1").val();
					var result2 = $("#inputBox2").val();
                    $(this).dialog("close");
					callback(result1, result2);
                }
            }
        });
		$("#dialog").dialog("open");
	}
	
	function showUpload1Prompt(title, callback) {
		var result = "";
		$("#dialog").empty();
		$("#dialog").append('<fieldset>'
			+ '<input type="radio" id="replaceDelete" name="deletequestion" value="delete">'
			+ '<label for="replaceDelete">Delete old config when replacing Dreamling</label><br>'
			+ '<input type="radio" id="replaceNoDelete" name="deletequestion" value="nodelete">'
			+ '<label for="replaceNoDelete">Do not delete old config when replacing Dreamling</label><br> </fieldset>');
        $("#dialog").dialog({
			title: title,
            autoOpen: false,
            resizable: true,
            modal: true,
            buttons: {
                "Submit": function() {
					var result = $("#replaceDelete").prop("checked");
                    $(this).dialog("close");
					callback(result);
                }
            }
        });
		$("#dialog").dialog("open");
	}
	
	function showUpload2Prompt(title) {
		var result = "";
		$("#dialog").empty();
		$("#dialog").append("<form method=\"POST\" action=\"data\" enctype=\"multipart/form-data\">"
					+ "<input type=\"file\" name=\"file1\" id=\"file1\" class=\"horiz-center\"><br />"
					+ "<input type=\"submit\" id=\"upload\" value=\"Upload and Delete configs\">"
					+ "</form>");
        $("#dialog").dialog({
			title: title,
            autoOpen: false,
            resizable: true,
            modal: true,
			buttons: []
        });
		$("#dialog").dialog("open");
	}
	
	function showUpload3Prompt(title) {
		var result = "";
		$("#dialog").empty();
		$("#dialog").append("<form method=\"POST\" action=\"data\" enctype=\"multipart/form-data\">"
					+ "<input type=\"file\" name=\"file2\" id=\"file2\" class=\"horiz-center\"><br />"
					+ "<input type=\"submit\" id=\"uploadDelete\" value=\"Upload and keep configs\">"
					+ "</form>");
        $("#dialog").dialog({
			title: title,
            autoOpen: false,
            resizable: true,
            modal: true,
			buttons: []
        });
		$("#dialog").dialog("open");
	}
	
	//multiple files
	function showUpload4Prompt(title) {
		var result = "";
		$("#dialog").empty();
		$("#dialog").append("<form method=\"POST\" action=\"data\" enctype=\"multipart/form-data\">"
					+ "<input type=\"file\" name=\"file3\" id=\"file3\" class=\"horiz-center\" multiple><br />"
					+ "<input type=\"submit\" id=\"uploadScripts\" value=\"Upload\">"
					+ "</form>");
        $("#dialog").dialog({
			title: title,
            autoOpen: false,
            resizable: true,
            modal: true,
			buttons: []
        });
		$("#dialog").dialog("open");
	}
	
	function showUpload5Prompt(title) {
		var result = "";
		$("#dialog").empty();
		$("#dialog").append("<form method=\"POST\" action=\"data\" enctype=\"multipart/form-data\">"
					+ "<input type=\"file\" name=\"file4\" id=\"file4\" class=\"horiz-center\"><br />"
					+ "<input type=\"submit\" id=\"uploadBash\" value=\"Upload\">"
					+ "</form>");
        $("#dialog").dialog({
			title: title,
            autoOpen: false,
            resizable: true,
            modal: true,
			buttons: []
        });
		$("#dialog").dialog("open");
	}
	
	$("#startAllDreamlings").click(function() {
		clearContent();
		$(".sidebar-button").each(function() {
			var id = $(this).attr("id");
			if (id !== undefined && id !== "showAll") {
				var pk = "-1:501:" + id;
				var params2 = "pk=" + pk + "&value=0";
				var http2 = makeXMLHttpRequestObject();
				http2.onreadystatechange = function() {
					if (this.readyState == 4) {
						show(id);
					}
				}
				http2.open("POST", "/data", true);
				http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				http2.send(params2);
			}
		});
		initDynamicButtons();
	});
	
	$("#killAllDreamlings").click(function() {
		clearContent();
		$(".sidebar-button").each(function() {
			var id = $(this).attr("id");
			if (id !== undefined && id !== "showAll") {
				var pk = "-1:502:" + id;
				var params2 = "pk=" + pk + "&value=0";
				var http2 = makeXMLHttpRequestObject();
				http2.onreadystatechange = function() {
					if (this.readyState == 4) {
						show(id);
					}
				}
				http2.open("POST", "/data", true);
				http2.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
				http2.send(params2);
			}
		});
		initDynamicButtons();
	});
	
	$("#replaceClient").click(function() {
		showUpload1Prompt("Choose Config", function(result) {
			if (result) { //delete true
				showUpload2Prompt("Upload new Dreamling");
			} else { //delete false
				showUpload3Prompt("Upload new Dreamling");
			}
		});
	});
	
	$("#replaceScripts").click(function() {
		showUpload4Prompt("Choose Proxies");
	});
	
	$("#runBash").click(function() {
		showUpload5Prompt("Upload bash script to run.");
	});
	
	function clearContent() {
		$("#accounts").empty();
		$("#accounts").append('<div>Banned: <span id="bannedCount">0</span>/<span id="allCount">0</span></div>');
		$("#accounts").append("<button id=\"replaceBanned\">Replace all banned</button>");
		$("#accounts").append("<button id=\"replaceAllAcc\">Replace all acc</button>");
		$("#accounts").append("<button id=\"replaceAllProxies\">Replace all proxies</button>");
		$("#accounts").append("<button id=\"startLauncher\">Start Launchers</button><br /><br />");
		$("#accounts").append("<button id=\"replaceXY\">Replace all X with Y</button><br /><br />");
	}
	
	$(".sidebar-button").click(function() {
		var id = $(this).attr("id");
		if (id !== "showAll") {
			showOnly(id);
		} else { //showAll
			clearContent();
			var ids = "";
			$(".sidebar-button").each(function() {
				var id = $(this).attr("id");
				if (id !== undefined && id !== "showAll") {
					ids = ids + id + "DREAMLING_SEPARATOR";
				}
			});
			if (ids !== "") {
				ids = ids.substring(0, ids.length - 19); //remove last DREAMLING_SEPARATOR
				show(ids);
			}
			
			initDynamicButtons();
		}
		var selected = $(this);
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
	});
	
	
	$("#BEFORE_SIDEBAR").height($(window).height() - 123);
	
	
});

$(window).resize(function() {
	$("#BEFORE_SIDEBAR").height($(window).height() - 123);
});

// Scrolls to the selected menu item on the page
$(function() {
  $('a[href*=#]:not([href=#])').off("click").on("click", function() {
    if (location.pathname.replace(/^\//, '') == this.pathname.replace(/^\//, '') || location.hostname == this.hostname) {
      var target = $(this.hash);
      target = target.length ? target : $('[name=' + this.hash.slice(1) + ']');
      if (target.length) {
        $('html,body').animate({
          scrollTop: target.offset().top
        }, 1000);
        return false;
      }
    }
  });
});

function GetLatestReleaseInfo() {
  $.getJSON("https://api.github.com/repos/suewonjp/civilizer/releases/latest").done(function (release) {
    var asset = release.assets[0];
    var downloadCount = 0;
    for (var i = 0; i < release.assets.length; i++) {
      downloadCount += release.assets[i].download_count;
    }
    var oneHour = 60 * 60 * 1000;
    var oneDay = 24 * oneHour;
    var dateDiff = new Date() - new Date(asset.updated_at);
    var timeAgo;
    if (dateDiff < oneDay) {
      timeAgo = (dateDiff / oneHour).toFixed(1) + " hours ago";
    }
    else {
      timeAgo = (dateDiff / oneDay).toFixed(1) + " days ago";
    }
    var releaseInfo = release.name + " was updated " + timeAgo + " and downloaded " + downloadCount.toLocaleString() + " times.";
    $(".sharex-download").attr("href", asset.browser_download_url);
    $(".release-info").text(releaseInfo);
    $(".release-info").fadeIn("slow");
    $("#latest-release-btn").attr("href", asset.browser_download_url);
    $("#latest-source-btn").attr("href", release.zipball_url);
  });
}

// Map scrolling behaviour
$(document).ready(function() {
  GetLatestReleaseInfo();

  // Closes the sidebar menu
  $("#menu-close, #menu-toggle").on("click.sidebar", function(e) {
    $("#sidebar-wrapper").toggleClass("active");
    return false;
  });

  $(document).on("click.sidebar", function(e) {
    var sw = $("#sidebar-wrapper");
    if (sw.hasClass("active")) {
      sw.toggleClass("active");
      return false;
    }
  });

  $(".screenshot-item").on("click", function(e) {
    $("#full-image").show()
      .css("background", "url("+$(this).find("img").attr("src")+") no-repeat center center fixed")
      .off("click").on("click", function(e) {
        $(this).hide();
      })
  });

  $(window).on("load resize", function() {
    $("#full-image").css("height", window.innerHeight);
  });

  /*$('#map_iframe').addClass('scrolloff');*/
  /*$('#map').on('click', function () {*/
  /*$('#map_iframe').removeClass('scrolloff');*/
  /*});*/

  /*$('#map_iframe').mouseleave(function  () {*/
  /*$('#map_iframe').addClass('scrolloff');*/
  /*});*/
});

function vote(evt, el) {
  el.style.visibility = "hidden";

  new Image().src = el.href;

  evt.stopPropagation();

  return false;
}

function hideRec(el, closed) {
  var kids = document.querySelectorAll('.comment[parent="' + el.id + '"]');

  for (var i = 0; i < kids.length; i++) {
    if (closed) kids[i].classList.remove("hidden");
    else kids[i].classList.add("hidden");
    hideRec(kids[i], closed);
  }
}

function toggle(evt, el) {
  var closed = el.classList.contains("closed");
  var parent = el.closest(".comment");
  var content = parent.querySelectorAll(".comment-content");
  var kids = document.querySelectorAll('.comment[parent="' + parent.id + '"]');

  console.log(kids.length, el.id);

  for (var i = 1; i < content.length; i++) {
    if (closed) content[i].classList.remove("hidden");
    else content[i].classList.add("hidden");
  }

  hideRec(parent, closed);

  if (closed) {
    el.classList.remove("closed");
    el.textContent = "[-]";
  } else {
    el.classList.add("closed");
    el.textContent = "[+]";
  }
}

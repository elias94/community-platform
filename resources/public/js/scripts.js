function vote(evt, el) {
  el.style.visibility = "hidden";

  new Image().src = el.href;

  evt.stopPropagation();

  return false;
}

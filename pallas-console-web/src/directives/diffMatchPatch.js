/* eslint-disable */
import { diff_match_patch, DIFF_DELETE, DIFF_INSERT, DIFF_EQUAL } from 'diff-match-patch';

function factory() {
  const displayType = {
    INSDEL: 0,
    LINEDIFF: 1,
  };
  function diffClass(op) {
    switch (op) {
    case DIFF_INSERT: return 'ins';
    case DIFF_DELETE: return 'del';
    case DIFF_EQUAL: return 'match';
    }
  }
  function diffSymbol(op) {
    switch (op) {
    case DIFF_EQUAL: return ' ';
    case DIFF_INSERT: return '+';
    case DIFF_DELETE: return '-';
    }
  }
  function diffTag(op) {
    switch (op) {
    case DIFF_EQUAL: return 'span';
    case DIFF_INSERT: return 'ins';
    case DIFF_DELETE: return 'del';
    }
  }
  function diffAttrName(op) {
    switch (op) {
    case DIFF_EQUAL: return 'equal';
    case DIFF_INSERT: return 'insert';
    case DIFF_DELETE: return 'delete';
    }
  }
  function isEmptyObject(o) {
    return Object.getOwnPropertyNames(o).length === 0;
  }
  function getTagAttrs(options, op, attrs) {
    attrs = attrs || {};
    let tagOptions = {};
    if (options && options.attrs) {
      tagOptions = JSON.parse(JSON.stringify(options.attrs[diffAttrName(op)] || {}));
    }
    if (isEmptyObject(tagOptions) && isEmptyObject(attrs)) {
      return '';
    }
    for (const k in attrs) {
      if (tagOptions[k]) {
        // The attribute defined in attrs should be first
        tagOptions[k] = `${attrs[k]} ${tagOptions[k]}`;
      } else {
        tagOptions[k] = attrs[k];
      }
    }
    const lis = [];
    for (const k in tagOptions) {
      lis.push(`${k}="${tagOptions[k]}"`);
    }
    return ` ${lis.join(' ')}`;
  }
  function getHtmlPrefix(op, display, options) {
    let retVal = '';
    switch (display) {
    case displayType.LINEDIFF:
      retVal = `<div class="${diffClass(op)}"><span${getTagAttrs(options, op, { class: 'noselect' })}>${diffSymbol(op)}</span>`;
      break;
    case displayType.INSDEL:
      const tag = diffTag(op);

      retVal = `<${tag}${getTagAttrs(options, op)}>`;
      break;
    }
    return retVal;
  }
  function getHtmlSuffix(op, display) {
    let retVal = '';
    switch (display) {
    case displayType.LINEDIFF:
      retVal = '</div>';
      break;
    case displayType.INSDEL:
      retVal = `</${diffTag(op)}>`;
      break;
    }
    return retVal;
  }
  function createHtmlLines(text, op, options) {
    const lines = text.split('\n');
    for (let y = 0; y < lines.length; y++) {
      if (lines[y].length === 0) continue;
      lines[y] = getHtmlPrefix(op, displayType.LINEDIFF, options) + lines[y] + getHtmlSuffix(op, displayType.LINEDIFF);
    }
    return lines.join('');
  }
  function createHtmlFromDiffs(diffs, display, options) {
    const pattern_amp = /&/g;
    const pattern_lt = /</g;
    const pattern_gt = />/g;
    for (let x = 0; x < diffs.length; x++) {
      const data = diffs[x][1];
      const text = data.replace(pattern_amp, '&amp;')
        .replace(pattern_lt, '&lt;')
        .replace(pattern_gt, '&gt;');
      diffs[x][1] = text;
    }

    const html = [];
    for (let x = 0; x < diffs.length; x++) {
      const op = diffs[x][0];
      const text = diffs[x][1];
      if (display === displayType.LINEDIFF) {
        html[x] = createHtmlLines(text, op, options);
      } else {
        html[x] = getHtmlPrefix(op, display, options) + text + getHtmlSuffix(op, display);
      }
    }
    return html.join('');
  }
  function assertArgumentsIsStrings(left, right) {
    return (typeof left === 'string') && (typeof right === 'string');
  }
  return {
    createDiffHtml(left, right, options) {
      if (assertArgumentsIsStrings(left, right)) {
        const dmp = new diff_match_patch();
        const diffs = dmp.diff_main(left, right);
        return createHtmlFromDiffs(diffs, displayType.INSDEL, options);
      }
      return '';
    },
    createProcessingDiffHtml(left, right, options) {
      if (assertArgumentsIsStrings(left, right)) {
        const dmp = new diff_match_patch();
        const diffs = dmp.diff_main(left, right);
        // dmp.Diff_EditCost = 4;
        dmp.diff_cleanupEfficiency(diffs);
        return createHtmlFromDiffs(diffs, displayType.INSDEL, options);
      }
      return '';
    },
    createSemanticDiffHtml(left, right, options) {
      if (assertArgumentsIsStrings(left, right)) {
        const dmp = new diff_match_patch();
        const diffs = dmp.diff_main(left, right);
        dmp.diff_cleanupSemantic(diffs);
        return createHtmlFromDiffs(diffs, displayType.INSDEL, options);
      }
      return '';
    },
    createLineDiffHtml(left, right, options) {
      if (assertArgumentsIsStrings(left, right)) {
        const dmp = new diff_match_patch();
        const a = dmp.diff_linesToChars_(left, right);
        const diffs = dmp.diff_main(a.chars1, a.chars2, false);
        dmp.diff_charsToLines_(diffs, a.lineArray);
        return createHtmlFromDiffs(diffs, displayType.LINEDIFF, options);
      }
      return '';
    },
  };
}

const dmp = factory();

export default {
  diff(el, binding) {
    const scope = binding.value;
    el.innerHTML = dmp.createDiffHtml(scope.left, scope.right, scope.options);
  },
  processingDiff(el, binding) {
    const scope = binding.value;
    el.innerHTML = dmp.createProcessingDiffHtml(scope.left, scope.right, scope.options);
  },
  semanticDiff(el, binding) {
    const scope = binding.value;
    el.innerHTML = dmp.createSemanticDiffHtml(scope.left, scope.right, scope.options);
  },
  lineDiff(el, binding) {
    console.log(binding);
    const scope = binding.value;
    el.innerHTML = dmp.createLineDiffHtml(scope.left, scope.right, scope.options);
  },
};


function StyleComposer(baseStyle: string = '', additionalStyles: Record<string, boolean> = {}) {
    let style = baseStyle;

    for (const [styleAddon, isUsing] of Object.entries(additionalStyles)) {
        if (styleAddon === 'undefined' || !isUsing) {
            continue
        }
        style += ` ${styleAddon}`;
    }

    return style.trim();
}

export default StyleComposer;
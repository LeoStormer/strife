
export const hexToRgba = (hex: string, alpha: number): string => {
    let normalizedHex = hex.replace('#', '');

    // If it's a shorthand hex (e.g., #abc), convert to full form (e.g., #aabbcc)
    if (normalizedHex.length === 3) {
        normalizedHex = normalizedHex.split('').map(c => c + c).join('');
    }

    const r = parseInt(normalizedHex.substring(0, 2), 16);
    const g = parseInt(normalizedHex.substring(2, 4), 16);
    const b = parseInt(normalizedHex.substring(4, 6), 16);

    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
};

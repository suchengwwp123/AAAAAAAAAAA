// ... existing code ...
import { clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs) {
    return twMerge(clsx(inputs));
}

// Removed type definition: export type ObjectValues<T> = T[keyof T];

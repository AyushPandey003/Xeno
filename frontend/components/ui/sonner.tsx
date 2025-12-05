"use client"

import { Toaster as Sonner } from "sonner"

type ToasterProps = React.ComponentProps<typeof Sonner>

const Toaster = ({ ...props }: ToasterProps) => {
    return (
        <Sonner
            theme="dark"
            className="toaster group"
            toastOptions={{
                classNames: {
                    toast:
                        "group toast group-[.toaster]:bg-gradient-to-br group-[.toaster]:from-white/10 group-[.toaster]:to-white/5 group-[.toaster]:backdrop-blur-xl group-[.toaster]:text-white group-[.toaster]:border-white/10 group-[.toaster]:shadow-xl",
                    description: "group-[.toast]:text-white/60",
                    actionButton:
                        "group-[.toast]:bg-violet-600 group-[.toast]:text-white",
                    cancelButton:
                        "group-[.toast]:bg-white/10 group-[.toast]:text-white",
                },
            }}
            {...props}
        />
    )
}

export { Toaster }

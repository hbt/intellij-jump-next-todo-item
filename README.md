## What is it?

Jump between todos in your code (like bookmarked locations).

## Why?

* Used to plan a patch of work and jump between file locations by placing todo items and jumping in order
* This is better than bookmarks since bookmarks lose the location after editing and require more work to sort


## Usage

* add todo items in code using format `// TODO(hbt) NEXT`
* add numbered items e.g `// TODO(hbt) NEXT 2 fix this` `// TODO(hbt) NEXT 4 then this`
* handles sub numbers like versioning e.g `// TODO(hbt) NEXT 2.2.3 forgot to do this first`
* map actions to keys 
```
nnoremap gj :action com.hbt.todos.next.PreviousToDoAction<CR>
nnoremap gk :action com.hbt.todos.next.NextToDo<CR>
```
* jump between todos in order

## Limitations

* Only works with my todo pattern `// TODO(hbt) NEXT` for now. Fork away.

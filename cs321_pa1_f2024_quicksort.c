#include <stdio.h>

#define N 512


/* Searches array a between the current i and the last index in the partition
 * l looking for the next element less than the partition value p, returning
 * its index or l + 1.
 */
int next_i(int *a, int i, int l, int p)
{
  while ((i <= l) && (a[i] < p)) {
    i++;
  }

  return i;
}

/* Searches array a in reverse order between the current j and the
 * first index in the partition f looking for the next earlier element
 * greater than or equal to the partition value p, returning its index
 * or f.
 */
int next_j(int *a, int j, int f, int p)
{
  while ((j > f) && (a[j] >= p)) {
    j--;
  }

  return j;
}

/* Swaps the value in a at index i with the value at index j. */
void swap(int *a, int i, int j)
{
  int tmp;

  tmp = a[i];
  a[i] = a[j];
  a[j] = tmp;
}

/* Partitions the elements of array a between indices f and l around
   the partition value a[f].
 */
int partition(int *a, int f, int l)
{
  int i, j, p, t;

  p = a[f];
  i = f + 1;
  j = l;
  
  while (i <= j) {
    i = next_i(a, i, l, p);
    j = next_j(a, j, f, p);

    if (i < j) {
      swap(a, i, j);
    }
  }
  
  if (j != f) {
    swap(a, j, f);
  }

  return j;
}

/* Calls partition to break the array a between f and l into two subarrays
 * and recursively calls quicksort_recurse on those.  Recursion bottoms out
 * on arrays of 0 or 1 element, which are trivially sorted.
 */
void quicksort_recurse(int *a, int f, int l)
{
  int p;

  if (f >= l) {
    return;
  }

  p = partition(a, f, l);
  quicksort_recurse(a, f, p - 1);
  quicksort_recurse(a, p + 1, l);
}

/* Helper to call quicksort_recurse correctly without presenting a strange
 * interface to users.
 */
void quicksort(int *a, int n)
{
  quicksort_recurse(a, 0, n - 1);
}

/* fill fills the array a (of n elements) with decreasing values from *
 * n - 1 to zero (reverse sorted order).                              */
void fill(int *a, int n) {
  int i;
  
  for (i = 0; i < n; i++) {
    a[i] = n - i - 1;
  }
}

/* Your main function should allocate space for an array, call fill to   *
 * fill it with decreasing numbers, and then call quicksort to sort      *
 * it.  Use the HALT emulator instruction to see the memory contents and *
 * confirm that your functions work.  You may choose any array size you  *
 * like (up to the default limit of memory, 4096 bytes or 512 8-byte     *
 * integers).                                                            *
 *                                                                       * 
 * After completing all of the above, HALT the emulator to force a core  *
 * dump so that you (and the TAs) can examine the contents of memory.    *
 *                                                                       */
int main(int argc, char *argv[])
{
  /* In your LEGv8 program, main will not be a procedure.  Control will *
   * begin at the top of the file, so you should think of that as main. *
   * If control reaches the end of the file, the program will exit,     *
   * which you may think of as leaving main.                            */

  int a[N];

  fill(a, N);

  /*
  int i;
  for (i = 0; i < N; i++) {
    printf("%d\t", a[i]);
  }
  printf("\n");
  */
  
  quicksort(a, N);

  /*
  for (i = 0; i < N; i++) {
    printf("%d\t", a[i]);
  }
  printf("\n");
  */
  
  return 0;
}
